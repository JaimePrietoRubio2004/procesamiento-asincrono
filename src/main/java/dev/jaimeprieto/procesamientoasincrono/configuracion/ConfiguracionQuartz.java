package dev.jaimeprieto.procesamientoasincrono.configuracion;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.jaimeprieto.procesamientoasincrono.planificador.RevisarTareasProgramadasJob;

/**
 * Registra el Job de revisión de tareas programadas y su Trigger periódico
 * (cada 30s) para que Quartz lo ejecute automáticamente.
 */
@Configuration
public class ConfiguracionQuartz {

	private static final String REVISAR_TAREAS_PROGRAMADAS_TRIGGER = "revisarTareasProgramadasTrigger";
	private static final String REVISAR_TAREAS_PROGRAMADAS = "revisarTareasProgramadas";

	/**
	 * Define el Job de forma durable (persiste en QRTZ_JOB_DETAILS aunque no tenga
	 * ningún Trigger asociado en ese momento).
	 */
	@Bean
	public JobDetail revisarTareasProgramadasJobDetails() {
		return JobBuilder.newJob(RevisarTareasProgramadasJob.class).withIdentity(REVISAR_TAREAS_PROGRAMADAS)
				.storeDurably().build();
	}

	/**
	 * Dispara el Job anterior cada 30 segundos, de forma indefinida.
	 */
	@Bean
	public Trigger revisarTareasProgramadasTrigger(JobDetail revisarTareasProgramadasJobDetails) {
		return TriggerBuilder.newTrigger().forJob(revisarTareasProgramadasJobDetails)
				.withIdentity(REVISAR_TAREAS_PROGRAMADAS_TRIGGER)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()).build();
	}
}
