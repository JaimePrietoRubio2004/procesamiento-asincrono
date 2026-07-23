package dev.jaimeprieto.procesamientoasincrono.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dev.jaimeprieto.procesamientoasincrono.dto.TareaDto;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioTarea;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@ExtendWith(MockitoExtension.class)
public class TareaServicioTest {

	private static final String TEST_TEST_COM = "test@test.com";

	private static final String DESTINATARIO = "destinatario";

	private static final String TAREAS_INTERCAMBIO = "tareas.intercambio";

	private static final String ENVIO_EMAIL = "ENVIO_EMAIL";

	@Mock
	private RepositorioTarea repositorioTarea;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Mock
	private MeterRegistry meterRegistry;

	@InjectMocks
	private TareaServicio tareaServicio;

	@Test
	void crearTarea_conDatosValidos_devuelveAceptadoYPublicaRabbit() {
		TareaDto dto = new TareaDto();
		dto.setTipo(ENVIO_EMAIL);
		dto.setPayload(Map.of(DESTINATARIO, TEST_TEST_COM));
		when(repositorioTarea.save(any(Tarea.class))).thenAnswer(invocacion -> {
			Tarea tarea = invocacion.getArgument(0);
			if (tarea.getId() == null) {
				tarea.setId(UUID.randomUUID());
			}
			return tarea;
		});
		when(meterRegistry.counter(anyString())).thenReturn(mock(Counter.class));
		ResponseEntity<Object> respuesta = tareaServicio.crearTarea(dto);
		assertEquals(HttpStatus.ACCEPTED, respuesta.getStatusCode());
		verify(rabbitTemplate).convertAndSend(eq(TAREAS_INTERCAMBIO), anyString(), anyString());
	}
}
