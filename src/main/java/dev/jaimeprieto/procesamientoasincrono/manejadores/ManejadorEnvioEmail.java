package dev.jaimeprieto.procesamientoasincrono.manejadores;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.jaimeprieto.procesamientoasincrono.excepciones.ExcepcionNoReintentable;
import dev.jaimeprieto.procesamientoasincrono.excepciones.ExcepcionReintentable;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;

//Manejador del tipo ENVIO_EMAIL: valida el payload y simula el envío
@Component
public class ManejadorEnvioEmail implements ManejadorTarea {

	@Override
	public String getTipo() {
		return "ENVIO_EMAIL";
	}

	@Override
	public void manejar(Tarea tarea) {
		Map<String, Object> payload = tarea.getPayload();
		if (!payload.containsKey("destinatario")) {
			throw new ExcepcionNoReintentable("Falta el campo 'destinatario' en el payload");
		}
		String destinatario = (String) payload.get("destinatario");
		// Simulación de fallo transitorio para poder probar el flujo de reintentos
		if ("fallar@test.com".equals(destinatario)) {
			throw new ExcepcionReintentable("Timeout simulado conectando con el proveedor de email");
		}
		System.out.println("Email enviado a: " + destinatario);
	}

}
