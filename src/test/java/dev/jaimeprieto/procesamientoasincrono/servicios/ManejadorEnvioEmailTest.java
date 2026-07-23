package dev.jaimeprieto.procesamientoasincrono.servicios;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.jaimeprieto.procesamientoasincrono.excepciones.ExcepcionNoReintentable;
import dev.jaimeprieto.procesamientoasincrono.excepciones.ExcepcionReintentable;
import dev.jaimeprieto.procesamientoasincrono.manejadores.ManejadorEnvioEmail;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;

public class ManejadorEnvioEmailTest {

	private static final String FALLAR_TEST_COM = "fallar@test.com";
	
	private static final String CLIENTE_TEST_COM = "cliente@test.com";
	
	private static final String DESTINATARIO = "destinatario";
	
	private final ManejadorEnvioEmail manejador = new ManejadorEnvioEmail();

	@Test
	void manejar_sinDestinatario_lanzaExcepcionNoReintentable() {
		Tarea tarea = new Tarea();
		tarea.setPayload(Map.of());

		assertThrows(ExcepcionNoReintentable.class, () -> manejador.manejar(tarea));
	}

	@Test
	void manejar_conDestinatario_lanzaExcepcionReintentable() {
		Tarea tarea = new Tarea();
		tarea.setPayload(Map.of(DESTINATARIO, FALLAR_TEST_COM));
		assertThrows(ExcepcionReintentable.class, () -> manejador.manejar(tarea));
	}
	
	@Test
	void manejar_conDestinatario_noLanzaExcepcion() {
		Tarea tarea = new Tarea();
		tarea.setPayload(Map.of(DESTINATARIO, CLIENTE_TEST_COM));
	}
}
