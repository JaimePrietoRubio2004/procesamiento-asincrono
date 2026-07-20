package dev.jaimeprieto.procesamientoasincrono.manejadores;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.jaimeprieto.procesamientoasincrono.excepciones.ExcepcionNoReintentable;

// Agrupa todos los ManejadorTarea disponibles, indexados por tipo, para resolverlos en tiempo de ejecución
@Component
public class RegistroManejadores {

	private final Map<String, ManejadorTarea> manejadores;

	// Construye el mapa tipo → manejador a partir de todos los ManejadorTarea que
	// Spring detecte
	public RegistroManejadores(List<ManejadorTarea> manejador) {
		this.manejadores = manejador.stream().collect(Collectors.toMap(ManejadorTarea::getTipo, m -> m));
	}

	// Devuelve el manejador correspondiente al tipo, o lanza excepción no
	// reintentable si no existe ninguno
	public ManejadorTarea obtener(String tipo) {
		ManejadorTarea manejadorTarea = manejadores.get(tipo);
		if (manejadorTarea == null) {
			throw new ExcepcionNoReintentable("No hay manejador registrado para el tipo: " + tipo);
		}
		return manejadorTarea;
	}
}
