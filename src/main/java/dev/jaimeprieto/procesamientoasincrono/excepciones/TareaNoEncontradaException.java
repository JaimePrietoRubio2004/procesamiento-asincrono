package dev.jaimeprieto.procesamientoasincrono.excepciones;

import java.util.UUID;

public class TareaNoEncontradaException extends RuntimeException {

	public TareaNoEncontradaException(UUID idTarea) {
		super("No se encontró la tarea con id: " + idTarea);
	}

}
