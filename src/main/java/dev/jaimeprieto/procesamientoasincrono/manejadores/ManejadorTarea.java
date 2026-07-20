package dev.jaimeprieto.procesamientoasincrono.manejadores;

import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;

//Contrato que debe implementar cada manejador de tarea, uno por cada tipo distinto
public interface ManejadorTarea {
	// Identifica qué tipo de tarea procesa esta implementación (debe coincidir con
	// Tarea.tipo)
	String getTipo();

	// Ejecuta la lógica de negocio específica para este tipo de tarea
	void manejar(Tarea tarea);

}
