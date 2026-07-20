package dev.jaimeprieto.procesamientoasincrono.configuracion;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionRabbitMQ {

	private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
	private static final String TAREAS_DLQ = "tareas.dlq";
	private static final String TAREAS_DLX = "tareas.dlx";
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
	 * Cola de tareas con prioridad alta; los mensajes rechazados se redirigen al
	 * DLX (tareas.dlq)
	 * 
	 * @return
	 */
	@Bean
	public Queue colaAlta() {
		return QueueBuilder.durable(TAREAS_ALTA).withArgument(X_DEAD_LETTER_EXCHANGE, TAREAS_DLX)
				.withArgument(X_DEAD_LETTER_ROUTING_KEY, TAREAS_DLQ).build();
	}

	/**
	 * Cola de tareas con prioridad media; los mensajes rechazados se redirigen al
	 * DLX (tareas.dlq)
	 * 
	 * @return
	 */
	@Bean
	public Queue colaMedia() {
		return QueueBuilder.durable(TAREAS_MEDIA).withArgument(X_DEAD_LETTER_EXCHANGE, TAREAS_DLX)
				.withArgument(X_DEAD_LETTER_ROUTING_KEY, TAREAS_DLQ).build();
	}

	/**
	 * Cola de tareas con prioridad baja; los mensajes rechazados se redirigen al
	 * DLX (tareas.dlq)
	 * 
	 * @return
	 */
	@Bean
	public Queue colaBaja() {
		return QueueBuilder.durable(TAREAS_BAJA).withArgument(X_DEAD_LETTER_EXCHANGE, TAREAS_DLX)
				.withArgument(X_DEAD_LETTER_ROUTING_KEY, TAREAS_DLQ).build();
	}

	/**
	 * Conecta colaAlta al intercambio con routing key "tareas.alta"
	 * 
	 * @param colaAlta
	 * @param tareasExchange
	 * @return
	 */
	@Bean
	public Binding bindingAlta(Queue colaAlta, @Qualifier("tareasExchange") DirectExchange tareasIntercambio) {
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
	public Binding bindingMedia(Queue colaMedia, @Qualifier("tareasExchange") DirectExchange tareasIntercambio) {
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
	public Binding bindingBaja(Queue colaBaja, @Qualifier("tareasExchange") DirectExchange tareasIntercambio) {
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

	// Exchange dedicado a mensajes muertos: tareas que agotaron sus reintentos
	@Bean
	public DirectExchange dlxExchange() {
		return new DirectExchange(TAREAS_DLX);
	}

	// Cola donde se acumulan las tareas fallidas definitivamente, para
	// inspección/reintento manual
	@Bean
	public Queue colaDlq() {
		return new Queue(TAREAS_DLQ);
	}

	// Conecta la cola de mensajes muertos (colaDlq) al exchange dlxExchange
	@Bean
	public Binding archivoDlq(Queue colaDlq, @Qualifier("dlxExchange") DirectExchange dlxIntercambio) {
		return BindingBuilder.bind(colaDlq).to(dlxIntercambio).with(TAREAS_DLQ);
	}
}
