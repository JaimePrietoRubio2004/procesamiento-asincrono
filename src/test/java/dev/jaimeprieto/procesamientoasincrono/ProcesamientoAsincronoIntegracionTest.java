package dev.jaimeprieto.procesamientoasincrono;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dev.jaimeprieto.procesamientoasincrono.dto.TareaDto;
import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioTarea;
import dev.jaimeprieto.procesamientoasincrono.servicios.TareaServicio;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ProcesamientoAsincronoIntegracionTest {

	private static final String DESTINATARIO = "destinatario";

	private static final String ENVIO_EMAIL = "ENVIO_EMAIL";

	private static final String RABBITMQ_3_MANAGEMENT = "rabbitmq:3-management";

	private static final String PROCESAMIENTO_ASINCRONO_TEST = "procesamiento_asincrono_test";

	private static final String POSTGRES_16 = "postgres:16";

	@Autowired
	private TareaServicio tareaServicio;

	@Autowired
	private RepositorioTarea repositorioTarea;

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_16)
			.withDatabaseName(PROCESAMIENTO_ASINCRONO_TEST);

	@Container
	static RabbitMQContainer rabbitmq = new RabbitMQContainer(RABBITMQ_3_MANAGEMENT);

	@DynamicPropertySource
	static void configurarPropiedades(DynamicPropertyRegistry registrar) {
		registrar.add("spring.datasource.url", postgres::getJdbcUrl);
		registrar.add("spring.datasource.username", postgres::getUsername);
		registrar.add("spring.datasource.password", postgres::getPassword);

		registrar.add("spring.rabbitmq.host", rabbitmq::getHost);
		registrar.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
		registrar.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
		registrar.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
	}

	@Test
	void tareaConDestinatarioValido_terminaCompletada() {
		TareaDto dto = new TareaDto();
		dto.setTipo(ENVIO_EMAIL);
		dto.setPayload(Map.of(DESTINATARIO, "integracion@test.com"));

		ResponseEntity<Object> respuesta = tareaServicio.crearTarea(dto);
		assertEquals(HttpStatus.ACCEPTED, respuesta.getStatusCode());

		TareaDto creada = (TareaDto) respuesta.getBody();

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			Tarea tarea = repositorioTarea.findById(creada.getIdTarea()).orElseThrow();
			assertEquals(EstadoTarea.COMPLETADA, tarea.getEstado());
		});
	}

	@Test
	void tareaConDestinatarioDePrueba_agotaReintentosYQuedaFallida() {
		TareaDto dto = new TareaDto();
		dto.setTipo("ENVIO_EMAIL");
		dto.setPayload(Map.of(DESTINATARIO, "fallar@test.com"));
		dto.setMaxReintentos(1);

		ResponseEntity<Object> respuesta = tareaServicio.crearTarea(dto);
		assertEquals(HttpStatus.ACCEPTED, respuesta.getStatusCode());

		TareaDto creada = (TareaDto) respuesta.getBody();

		await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
			Tarea tarea = repositorioTarea.findById(creada.getIdTarea()).orElseThrow();
			assertEquals(EstadoTarea.FALLIDA, tarea.getEstado());
		});
	}
}
