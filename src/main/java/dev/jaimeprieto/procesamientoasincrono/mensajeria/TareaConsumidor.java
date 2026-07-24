package dev.jaimeprieto.procesamientoasincrono.mensajeria;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import dev.jaimeprieto.procesamientoasincrono.excepciones.ExcepcionReintentable;
import dev.jaimeprieto.procesamientoasincrono.manejadores.ManejadorTarea;
import dev.jaimeprieto.procesamientoasincrono.manejadores.RegistroManejadores;
import dev.jaimeprieto.procesamientoasincrono.modelos.EjecucionTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoEjecucion;
import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioEjecucionTarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioTarea;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class TareaConsumidor {

	private static final String ID_TAREA = "idTarea";

	private static final String TIPO = "tipo";

	private static final String TAREAS_TIEMPO_PROCESAMIENTO = "tareas.tiempo.procesamiento";

	private static final String TAREAS_REINTENTADAS = "tareas.reintentadas";

	private static final String TAREAS_FALLIDAS = "tareas.fallidas";

	private static final String TAREAS_COMPLETADAS = "tareas.completadas";

	private static final String FACTORIA_BAJA = "bajaFactoria";

	private static final String FACTORIA_MEDIA = "mediaFactoria";

	private static final String FACTORIA_ALTA = "altaFactoria";

	private static final String TAREAS_BAJA = "tareas.baja";

	private static final String TAREAS_MEDIA = "tareas.media";

	private static final String TAREAS_ALTA = "tareas.alta";

	private final RepositorioTarea repositorioTarea;

	private final RepositorioEjecucionTarea repositorioEjecucionTarea;

	private final RegistroManejadores registroManejadores;

	private final MeterRegistry meterRegistry;

	public TareaConsumidor(RepositorioTarea repositorioTarea, RegistroManejadores registroManejadores,
			MeterRegistry meterRegistry, RepositorioEjecucionTarea repositorioEjecucionTarea) {
		this.repositorioTarea = repositorioTarea;
		this.registroManejadores = registroManejadores;
		this.meterRegistry = meterRegistry;
		this.repositorioEjecucionTarea = repositorioEjecucionTarea;
	}

	// Consume mensajes de la cola de prioridad alta (prefetch alto)
	@RabbitListener(queues = TAREAS_ALTA, containerFactory = FACTORIA_ALTA)
	public void procesarTareaAlta(String idTarea) {
		procesarTarea(idTarea);
	}

	// Consume mensajes de la cola de prioridad media
	@RabbitListener(queues = TAREAS_MEDIA, containerFactory = FACTORIA_MEDIA)
	public void procesarTareaMedia(String idTarea) {
		procesarTarea(idTarea);
	}

	// Consume mensajes de la cola de prioridad baja (prefetch bajo)
	@RabbitListener(queues = TAREAS_BAJA, containerFactory = FACTORIA_BAJA)
	public void procesarTareaBaja(String idTarea) {
		procesarTarea(idTarea);
	}

	// Busca la tarea, la marca PROCESANDO, ejecuta su manejador con reintentos y
	// backoff exponencial
	// (según maxReintentos de la tarea); si agota los intentos o el fallo no es
	// reintentable,
	// marca la tarea FALLIDA y rechaza el mensaje sin reencolar (cae al DLQ); si
	// todo va bien, la marca COMPLETADA
	// Se engancha a los eventos de Resilience4j para registrar en ejecucion_tarea
	// cada intento real (reintentable, exitoso o fallido definitivo) y actualizar
	// métricas.
	public void procesarTarea(String idTarea) {
		try {
			MDC.put(ID_TAREA, idTarea);
			String idLimpio = idTarea.replaceAll("^\"|\"$", "");
			UUID id = UUID.fromString(idLimpio);
			Tarea tarea = repositorioTarea.findById(id).orElseThrow();
			iniciarProcesamiento(tarea);

			ManejadorTarea manejador = registroManejadores.obtener(tarea.getTipo());

			RetryConfig config = RetryConfig.custom().maxAttempts(tarea.getMaxReintentos())
					.intervalFunction(IntervalFunction.ofExponentialBackoff(2000, 2))
					.retryExceptions(ExcepcionReintentable.class).build();
			Retry retry = Retry.of("tarea-" + tarea.getId(), config);
			retry.getEventPublisher().onRetry(evento -> {
				meterRegistry.counter(TAREAS_REINTENTADAS).increment();
				registrarEjecucion(tarea, evento.getNumberOfRetryAttempts(), EstadoEjecucion.FALLIDO_REINTENTABLE,
						evento.getLastThrowable().getMessage());
			}).onSuccess(evento -> registrarEjecucion(tarea, evento.getNumberOfRetryAttempts() + 1,
					EstadoEjecucion.EXITO, null))
					.onError(evento -> registrarEjecucion(tarea, evento.getNumberOfRetryAttempts(),
							EstadoEjecucion.FALLIDO_FINAL, evento.getLastThrowable().getMessage()));
			Timer.Sample muestra = Timer.start(meterRegistry);
			try {
				retry.executeRunnable(() -> manejador.manejar(tarea));
			} catch (Exception e) {
				tarea.setEstado(EstadoTarea.FALLIDA);
				tarea.setMensajeError(e.getMessage());
				repositorioTarea.save(tarea);
				meterRegistry.counter(TAREAS_FALLIDAS).increment();
				throw new AmqpRejectAndDontRequeueException("Tarea fallida definitivamente: " + idTarea, e);
			} finally {
				muestra.stop(
						Timer.builder(TAREAS_TIEMPO_PROCESAMIENTO).tag(TIPO, tarea.getTipo()).register(meterRegistry));
				MDC.remove(ID_TAREA);
			}
			completarTarea(tarea);
		} catch (AmqpRejectAndDontRequeueException e) {
			throw e;
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException("Error inesperado procesando mensaje: " + idTarea, e);
		}
	}

	// Marca la tarea como PROCESANDO y guarda el cambio
	private void iniciarProcesamiento(Tarea tarea) {
		tarea.setEstado(EstadoTarea.PROCESANDO);
		repositorioTarea.save(tarea);
	}

	/**
	 * Si la tarea es recurrente (expresionCron), recalcula la próxima ejecución y
	 * la vuelve a dejar en PROGRAMADA; si no, la marca como COMPLETADA.
	 */
	private void completarTarea(Tarea tarea) {
		if (tarea.getExpresionCron() != null) {
			CronExpression cron = CronExpression.parse(tarea.getExpresionCron());
			LocalDateTime siguiente = cron.next(tarea.getFechaProximaEjecucion());
			tarea.setContadorReintentos(0);
			tarea.setFechaProximaEjecucion(siguiente);
			tarea.setEstado(EstadoTarea.PROGRAMADA);
		} else {
			tarea.setEstado(EstadoTarea.COMPLETADA);
			tarea.setFechaFinalizacion(LocalDateTime.now());
			meterRegistry.counter(TAREAS_COMPLETADAS).increment();
		}
		repositorioTarea.save(tarea);
	}

	private void registrarEjecucion(Tarea tarea, int numeroIntento, EstadoEjecucion estado, String mensajeError) {
		EjecucionTarea ejecucion = new EjecucionTarea();
		ejecucion.setTarea(tarea);
		ejecucion.setNumeroIntento(numeroIntento);
		ejecucion.setEstado(estado);
		ejecucion.setMensajeError(mensajeError);
		ejecucion.setFechaInicio(LocalDateTime.now());
		ejecucion.setFechaFin(LocalDateTime.now());
		repositorioEjecucionTarea.save(ejecucion);
	}
}
