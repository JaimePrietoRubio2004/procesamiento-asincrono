package dev.jaimeprieto.procesamientoasincrono.planificador;

import java.time.LocalDateTime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.jaimeprieto.procesamientoasincrono.servicios.TareaServicio;

/**
 * Job de Quartz ejecutado periódicamente: delega en TareaServicio la búsqueda y
 * encolado de tareas cuya fechaProgramada ya se ha cumplido.
 */
@Component
public class RevisarTareasProgramadasJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(RevisarTareasProgramadasJob.class);

	private final TareaServicio tareaServicio;

	public RevisarTareasProgramadasJob(TareaServicio tareaServicio) {
		this.tareaServicio = tareaServicio;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Ejecutando revisión de tareas programadas: {}", LocalDateTime.now());
		tareaServicio.encolarTareasProgramadasVencidas();
	}

}
