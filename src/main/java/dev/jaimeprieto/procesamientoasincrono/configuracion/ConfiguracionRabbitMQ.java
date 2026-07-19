package dev.jaimeprieto.procesamientoasincrono.configuracion;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionRabbitMQ {

	private static final String TAREAS_BAJA = "tareas.baja";
	private static final String TAREAS_MEDIA = "tareas.media";
	private static final String TAREAS_ALTA = "tareas.alta";

	/**
	 * Exchange tipo direct que enruta las tareas según su prioridad
	 * 
	 * @return
	 */
	@Bean
	public DirectExchange tareasExchange() {
		return new DirectExchange("tareas.intercambio");
	}

	/**
	 * Cola de tareas con prioridad alta
	 * 
	 * @return
	 */
	@Bean
	public Queue colaAlta() {
		return new Queue(TAREAS_ALTA);
	}

	/**
	 * Cola de tareas con prioridad media
	 * 
	 * @return
	 */
	@Bean
	public Queue colaMedia() {
		return new Queue(TAREAS_MEDIA);
	}

	/**
	 * Cola de tareas con prioridad baja
	 * 
	 * @return
	 */
	@Bean
	public Queue colaBaja() {
		return new Queue(TAREAS_BAJA);
	}

	/**
	 * Conecta colaAlta al intercambio con routing key "tareas.alta"
	 * 
	 * @param colaAlta
	 * @param tareasExchange
	 * @return
	 */
	@Bean
	public Binding bindingAlta(Queue colaAlta, DirectExchange tareasIntercambio) {
		return BindingBuilder.bind(colaAlta).to(tareasIntercambio).with(TAREAS_ALTA);
	}

	/**
	 * Conecta colaAlta al intercambio con routing key "tareas.media"
	 * 
	 * @param colaMedia
	 * @param tareasExchange
	 * @return
	 */
	@Bean
	public Binding bindingMedia(Queue colaMedia, DirectExchange tareasIntercambio) {
		return BindingBuilder.bind(colaMedia).to(tareasIntercambio).with(TAREAS_MEDIA);
	}

	/**
	 * Conecta colaAlta al intercambio con routing key "tareas.baja"
	 * 
	 * @param colaBaja
	 * @param tareasExchange
	 * @return
	 */
	@Bean
	public Binding bindingBaja(Queue colaBaja, DirectExchange tareasIntercambio) {
		return BindingBuilder.bind(colaBaja).to(tareasIntercambio).with(TAREAS_BAJA);
	}

	/**
	 * Serializa los mensajes en JSON en vez de binario Java, para que sean legibles
	 * en la UI de RabbitMQ
	 * 
	 * @return
	 */
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory altaFactoria(ConnectionFactory conexionFactoria) {
		SimpleRabbitListenerContainerFactory factoria = new SimpleRabbitListenerContainerFactory();
		factoria.setConnectionFactory(conexionFactoria);
		factoria.setPrefetchCount(10);
		return factoria;
	}
	
	@Bean
	public SimpleRabbitListenerContainerFactory mediaFactoria(ConnectionFactory conexionFactoria) {
		SimpleRabbitListenerContainerFactory factoria = new SimpleRabbitListenerContainerFactory();
		factoria.setConnectionFactory(conexionFactoria);
		factoria.setPrefetchCount(5);
		return factoria;
	}
	
	@Bean
	public SimpleRabbitListenerContainerFactory bajaFactoria(ConnectionFactory conexionFactoria) {
		SimpleRabbitListenerContainerFactory factoria = new SimpleRabbitListenerContainerFactory();
		factoria.setConnectionFactory(conexionFactoria);
		factoria.setPrefetchCount(2);
		return factoria;
	}

}
