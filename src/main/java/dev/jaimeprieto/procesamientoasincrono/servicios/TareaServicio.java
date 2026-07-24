package dev.jaimeprieto.procesamientoasincrono.servicios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import dev.jaimeprieto.procesamientoasincrono.dto.EjecucionTareaDto;
import dev.jaimeprieto.procesamientoasincrono.dto.TareaDto;
import dev.jaimeprieto.procesamientoasincrono.excepciones.TareaNoEncontradaException;
import dev.jaimeprieto.procesamientoasincrono.modelos.EjecucionTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.PrioridadTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioEjecucionTarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioTarea;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class TareaServicio {

	private static final Logger log = LoggerFactory.getLogger(TareaServicio.class);

	private static final String TAREAS_CREADAS = "tareas.creadas";

	private static final String TAREAS_INTERCAMBIO = "tareas.intercambio";

	private static final String TAREAS_BAJA = "tareas.baja";

	private static final String TAREAS_MEDIA = "tareas.media";

	private static final String TAREAS_ALTA = "tareas.alta";

	private final RepositorioTarea repositorioTarea;

	private final RabbitTemplate rabbitTemplate;

	private final MeterRegistry meterRegistry;

	private final RepositorioEjecucionTarea repositorioEjecucionTarea;

	public TareaServicio(RepositorioTarea repositoriotarea, RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry,
			RepositorioEjecucionTarea repositorioEjecucionTarea) {
		this.repositorioTarea = repositoriotarea;
		this.rabbitTemplate = rabbitTemplate;
		this.meterRegistry = meterRegistry;
		this.repositorioEjecucionTarea = repositorioEjecucionTarea;
	}

	public ResponseEntity<Object> crearTarea(TareaDto tareaDto) {
		if (tareaDto == null) {
			return ResponseEntity.badRequest().build();
		}
		if (tareaDto.getPrioridad() == null) {
			tareaDto.setPrioridad(PrioridadTarea.MEDIA);
		}
		if (tareaDto.getMaxReintentos() == null) {
			tareaDto.setMaxReintentos(3); // Default
		}
		Tarea tarea = new Tarea();
		nuevaTarea(tareaDto, tarea);
		Tarea tareaGuardada = repositorioTarea.save(tarea);
		tareaGuardada = publicarMensajeYCambioEstado(tareaGuardada);
		meterRegistry.counter(TAREAS_CREADAS).increment();
		TareaDto nuevaTareaDto = new TareaDto();
		nuevaTareaDto.setIdTarea(tareaGuardada.getId());
		nuevaTareaDto.setEstado(tareaGuardada.getEstado());
		nuevaTareaDto.setFechaCreacion(tareaGuardada.getFechaCreacion());
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(nuevaTareaDto);
	}

	private Tarea publicarMensajeYCambioEstado(Tarea tareaGuardada) {
		if (tareaGuardada.getEstado().equals(EstadoTarea.PENDIENTE)) {
			String routingKey = switch (tareaGuardada.getPrioridad()) {
			case ALTA -> TAREAS_ALTA;
			case MEDIA -> TAREAS_MEDIA;
			case BAJA -> TAREAS_BAJA;
			};
			// publica un mensaje en RabbitMQ (internamente convierte un string en un
			// mensaje de AMQP:
			// 1º parametro --> A cual intercambio pertenece
			// 2º parametro --> Con que etiqueta se manda
			// 3º parametro --> El contenido
			rabbitTemplate.convertAndSend(TAREAS_INTERCAMBIO, routingKey, tareaGuardada.getId().toString());
			tareaGuardada.setEstado(EstadoTarea.ENCOLADA);
			tareaGuardada = repositorioTarea.save(tareaGuardada);
		}
		return tareaGuardada;
	}

	private void nuevaTarea(TareaDto tareaDto, Tarea tarea) {
		tarea.setTipo(tareaDto.getTipo());
		tarea.setPayload(tareaDto.getPayload());
		tarea.setPrioridad(tareaDto.getPrioridad());
		tarea.setContadorReintentos(0);
		tarea.setExpresionCron(tareaDto.getExpresionCron());
		tarea.setMaxReintentos(tareaDto.getMaxReintentos());
		tarea.setFechaProgramada(tareaDto.getFechaProgramada());
		tarea.setFechaCreacion(LocalDateTime.now());
		tarea.setFechaActualizacion(LocalDateTime.now());
		if (tareaDto.getExpresionCron() != null) {
			tarea.setEstado(EstadoTarea.PROGRAMADA);
			CronExpression cron = CronExpression.parse(tareaDto.getExpresionCron());
			tarea.setFechaProximaEjecucion(cron.next(LocalDateTime.now()));
		} else if (tareaDto.getFechaProgramada() != null
				&& tareaDto.getFechaProgramada().isAfter(LocalDateTime.now())) {
			tarea.setEstado(EstadoTarea.PROGRAMADA);
		} else {
			tarea.setEstado(EstadoTarea.PENDIENTE);
		}
	}

	public ResponseEntity<Object> consultarEstado(UUID idTarea) {
		if (idTarea == null) {
			return ResponseEntity.badRequest().build();
		}
		Tarea tarea = repositorioTarea.findById(idTarea).orElseThrow(() -> new TareaNoEncontradaException(idTarea));
		return ResponseEntity.status(HttpStatus.OK).body(convertirADto(tarea));
	}

	public ResponseEntity<Object> listarTareas(EstadoTarea estado, String tipo, Pageable pageable) {
		Page<Tarea> tareaDePaginas;
		if (estado != null && tipo != null) {
			tareaDePaginas = repositorioTarea.findByEstadoAndTipo(estado, tipo, pageable);
		} else if (estado != null) {
			tareaDePaginas = repositorioTarea.findByEstado(estado, pageable);
		} else if (tipo != null) {
			tareaDePaginas = repositorioTarea.findByTipo(tipo, pageable);
		} else {
			tareaDePaginas = repositorioTarea.findAll(pageable);
		}
		Page<TareaDto> dtoDePagina = tareaDePaginas.map(this::convertirADto);
		return ResponseEntity.ok(dtoDePagina);
	}

	private TareaDto convertirADto(Tarea tarea) {
		TareaDto dto = new TareaDto();
		dto.setIdTarea(tarea.getId());
		dto.setTipo(tarea.getTipo());
		dto.setEstado(tarea.getEstado());
		dto.setPrioridad(tarea.getPrioridad());
		dto.setContadorReintentos(tarea.getContadorReintentos());
		dto.setMaxReintentos(tarea.getMaxReintentos());
		dto.setFechaCreacion(tarea.getFechaCreacion());
		dto.setFechaActualizacion(tarea.getFechaActualizacion());
		return dto;
	}

	public ResponseEntity<Object> cancelarTarea(UUID idTarea) {
		if (idTarea == null) {
			return ResponseEntity.badRequest().build();
		}
		Tarea tarea = repositorioTarea.findById(idTarea).orElseThrow(() -> new TareaNoEncontradaException(idTarea));
		if (tarea.getEstado().equals(EstadoTarea.PENDIENTE) || tarea.getEstado().equals(EstadoTarea.PROGRAMADA)
				|| tarea.getEstado().equals(EstadoTarea.ENCOLADA)) {
			tarea.setEstado(EstadoTarea.CANCELADA);
			repositorioTarea.save(tarea);
			return ResponseEntity.ok("La tarea se ha cancelado correctamente");
		} else {
			EstadoTarea estado = tarea.getEstado();
			return ResponseEntity.status(HttpStatus.CONFLICT).body("La tarea esta en estado: " + estado);
		}
	}

	public ResponseEntity<Object> reintentoManual(UUID idTarea) {
		return reprocesarTareaFallida(idTarea);
	}

	private ResponseEntity<Object> reprocesarTareaFallida(UUID idTarea) {
		if (idTarea == null) {
			return ResponseEntity.badRequest().build();
		}
		Tarea tarea = repositorioTarea.findById(idTarea).orElseThrow(() -> new TareaNoEncontradaException(idTarea));
		if (tarea.getEstado() != EstadoTarea.FALLIDA) {
			EstadoTarea estado = tarea.getEstado();
			return ResponseEntity.status(HttpStatus.CONFLICT).body("La tarea esta en estado: " + estado);
		}
		tarea.setContadorReintentos(0);
		tarea.setMensajeError(null);
		tarea.setEstado(EstadoTarea.PENDIENTE);
		tarea = repositorioTarea.save(tarea);
		tarea = publicarMensajeYCambioEstado(tarea);
		return ResponseEntity.accepted().build();
	}

	public ResponseEntity<Object> reinyeccionManual(UUID idTarea) {
		return reprocesarTareaFallida(idTarea);
	}

	public ResponseEntity<Object> inspeccionMensajeDlq(Pageable pageable) {
		Page<Tarea> tareasFallidas = repositorioTarea.findByEstado(EstadoTarea.FALLIDA, pageable);
		Page<TareaDto> dtoTareasFallidas = tareasFallidas.map(this::convertirADto);
		return ResponseEntity.ok(dtoTareasFallidas);
	}

	/**
	 * Busca tareas en estado PROGRAMADA cuya fechaProgramada ya se ha cumplido y
	 * las encola (mismo camino que crearTarea: PENDIENTE -> publicar -> ENCOLADA).
	 */
	public void encolarTareasProgramadasVencidas() {
		List<Tarea> tareasProgramadas = repositorioTarea.findByEstadoAndFechaProgramadaBefore(EstadoTarea.PROGRAMADA,
				LocalDateTime.now());
		for (Tarea tarea : tareasProgramadas) {
			actualizarEstadoYPublicarMensaje(tarea);
		}
		List<Tarea> tareasRecurrentes = repositorioTarea
				.findByExpresionCronIsNotNullAndFechaProximaEjecucionBefore(LocalDateTime.now());
		for (Tarea tarea : tareasRecurrentes) {
			actualizarEstadoYPublicarMensaje(tarea);
		}
		log.info("Barrido de tareas programadas ({}): {} normales, {} recurrentes encoladas", LocalDateTime.now(),
				tareasProgramadas.size(), tareasRecurrentes.size());
	}

	private void actualizarEstadoYPublicarMensaje(Tarea tarea) {
		tarea.setEstado(EstadoTarea.PENDIENTE);
		Tarea guardada = repositorioTarea.save(tarea);
		publicarMensajeYCambioEstado(guardada);
	}

	public ResponseEntity<Object> consultarEjecucionTarea(UUID idTarea) {
		if (idTarea == null) {
			return ResponseEntity.badRequest().build();
		}
		if (!repositorioTarea.existsById(idTarea)) {
			throw new TareaNoEncontradaException(idTarea);
		}
		List<EjecucionTarea> listaEjecucionTarea = repositorioEjecucionTarea
				.findByTareaIdOrderByNumeroIntentoAsc(idTarea);
		List<EjecucionTareaDto> listaDto = listaEjecucionTarea.stream().map(this::convertirAEjecucionDto).toList();
		return ResponseEntity.ok(listaDto);
	}

	private EjecucionTareaDto convertirAEjecucionDto(EjecucionTarea ejecucion) {
		EjecucionTareaDto dto = new EjecucionTareaDto();
		dto.setNumeroIntento(ejecucion.getNumeroIntento());
		dto.setEstado(ejecucion.getEstado());
		dto.setMensajeError(ejecucion.getMensajeError());
		dto.setFechaInicio(ejecucion.getFechaInicio());
		dto.setFechaFin(ejecucion.getFechaFin());
		return dto;
	}
}
