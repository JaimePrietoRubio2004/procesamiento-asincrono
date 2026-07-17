package dev.jaimeprieto.procesamientoasincrono.controladores;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jaimeprieto.procesamientoasincrono.dto.TareaDto;
import dev.jaimeprieto.procesamientoasincrono.excepciones.TareaNoEncontradaException;
import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.servicios.TareaServicio;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TareaControlador {

	private static final Logger log = LoggerFactory.getLogger(TareaControlador.class);

	private final TareaServicio servicio;

	public TareaControlador(TareaServicio servicio) {
		this.servicio = servicio;
	}

	@PostMapping("/tareas")
	public ResponseEntity<Object> crearTarea(@Valid @RequestBody TareaDto tarea) {
		try {
			return servicio.crearTarea(tarea);
		} catch (Exception e) {
			log.error("Error al crear la tarea", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al crear la tarea");
		}
	}

	@GetMapping("/tareas/{idTarea}")
	public ResponseEntity<Object> consultarEstado(@PathVariable UUID idTarea) {
		try {
			return servicio.consultarEstado(idTarea);
		} catch (TareaNoEncontradaException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			log.error("Error al consultar el estado de la tarea", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error inesperado al consultar el estado de la tarea");
		}
	}

	@GetMapping("/tareas")
	public ResponseEntity<Object> listarTareas(@RequestParam(required = false) EstadoTarea estado,
			@RequestParam(required = false) String tipo, Pageable pageable) {
		try {
			return servicio.listarTareas(estado, tipo, pageable);
		} catch (Exception e) {
			log.error("Error al listar las tareas", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error inesperado al listar las tareas");
		}
	}

	@DeleteMapping("/tareas/{idTarea}")
	public ResponseEntity<Object> cancelarTarea(@PathVariable UUID idTarea) {
		try {
			return servicio.cancelarTarea(idTarea);
		} catch (TareaNoEncontradaException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			log.error("Error al cancelar la tarea", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error inesperado al cancelar la tarea");
		}
	}
}
