package dev.jaimeprieto.procesamientoasincrono.mensajeria;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioTarea;

@Component
public class TareaConsumidor {

	private static final String FACTORIA_BAJA = "bajaFactoria";

	private static final String FACTORIA_MEDIA = "mediaFactoria";

	private static final String FACTORIA_ALTA = "altaFactoria";

	private static final String TAREAS_BAJA = "tareas.baja";

	private static final String TAREAS_MEDIA = "tareas.media";

	private static final String TAREAS_ALTA = "tareas.alta";

	private final RepositorioTarea repositorioTarea;

	public TareaConsumidor(RepositorioTarea repositorioTarea) {
		this.repositorioTarea = repositorioTarea;
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

	// Busca la tarea por id y orquesta su procesamiento: inicia y completa
	public void procesarTarea(String idTarea) {
		UUID id = UUID.fromString(idTarea);
		Tarea tarea = repositorioTarea.findById(id).orElseThrow();
		iniciarProcesamiento(tarea);
		// Logica del dia 4
		completarTarea(tarea);
	}

	// Marca la tarea como PROCESANDO y guarda el cambio
	private void iniciarProcesamiento(Tarea tarea) {
		tarea.setEstado(EstadoTarea.PROCESANDO);
		repositorioTarea.save(tarea);
	}

	// Marca la tarea como COMPLETADA y guarda el cambio
	private void completarTarea(Tarea tarea) {
		tarea.setEstado(EstadoTarea.COMPLETADA);
		repositorioTarea.save(tarea);
	}
}
