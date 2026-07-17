package dev.jaimeprieto.procesamientoasincrono.servicios;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import dev.jaimeprieto.procesamientoasincrono.dto.TareaDto;
import dev.jaimeprieto.procesamientoasincrono.excepciones.TareaNoEncontradaException;
import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.PrioridadTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;
import dev.jaimeprieto.procesamientoasincrono.repositorios.RepositorioTarea;

@Service
public class TareaServicio {

	private final RepositorioTarea repositorioTarea;

	public TareaServicio(RepositorioTarea repositoriotarea) {
		this.repositorioTarea = repositoriotarea;
	}

	public ResponseEntity<Object> crearTarea(TareaDto tareaDto) {
		if (tareaDto == null) {
			return ResponseEntity.badRequest().build();
		}
		if (tareaDto.getPrioridad() == null) {
			tareaDto.setPrioridad(PrioridadTarea.MEDIA);
		}
		if (tareaDto.getMaxReintentos() == null) {
			tareaDto.setMaxReintentos(3); // Default
		}
		Tarea tarea = new Tarea();
		nuevaTarea(tareaDto, tarea);
		Tarea tareaGuardada = repositorioTarea.save(tarea);
		TareaDto nuevaTareaDto = new TareaDto();
		nuevaTareaDto.setIdTarea(tareaGuardada.getId());
		nuevaTareaDto.setEstado(tareaGuardada.getEstado());
		nuevaTareaDto.setFechaCreacion(tareaGuardada.getFechaCreacion());
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(nuevaTareaDto);
	}

	private void nuevaTarea(TareaDto tareaDto, Tarea tarea) {
		tarea.setTipo(tareaDto.getTipo());
		tarea.setPayload(tareaDto.getPayload());
		tarea.setPrioridad(tareaDto.getPrioridad());
		tarea.setContadorReintentos(0);
		tarea.setMaxReintentos(tareaDto.getMaxReintentos());
		tarea.setFechaProgramada(tareaDto.getFechaProgramada());
		tarea.setFechaCreacion(LocalDateTime.now());
		tarea.setFechaActualizacion(LocalDateTime.now());
		tarea.setEstado(
				tareaDto.getFechaProgramada() != null && tareaDto.getFechaProgramada().isAfter(LocalDateTime.now())
						? EstadoTarea.PROGRAMADA
						: EstadoTarea.PENDIENTE);
	}

	public ResponseEntity<Object> consultarEstado(UUID idTarea) {
		if (idTarea == null) {
			return ResponseEntity.badRequest().build();
		}
		Tarea tarea = repositorioTarea.findById(idTarea).orElseThrow(() -> new TareaNoEncontradaException(idTarea));
		return ResponseEntity.status(HttpStatus.OK).body(convertirADto(tarea));
	}

	public ResponseEntity<Object> listarTareas(EstadoTarea estado, String tipo, Pageable pageable) {
		Page<Tarea> tareaDePaginas;
		if (estado != null && tipo != null) {
			tareaDePaginas = repositorioTarea.findByEstadoAndTipo(estado, tipo, pageable);
		} else if (estado != null) {
			tareaDePaginas = repositorioTarea.findByEstado(estado, pageable);
		} else if (tipo != null) {
			tareaDePaginas = repositorioTarea.findByTipo(tipo, pageable);
		} else {
			tareaDePaginas = repositorioTarea.findAll(pageable);
		}
		Page<TareaDto> dtoDePagina = tareaDePaginas.map(this::convertirADto);
		return ResponseEntity.ok(dtoDePagina);
	}

	private TareaDto convertirADto(Tarea tarea) {
		TareaDto dto = new TareaDto();
		dto.setIdTarea(tarea.getId());
		dto.setTipo(tarea.getTipo());
		dto.setEstado(tarea.getEstado());
		dto.setPrioridad(tarea.getPrioridad());
		dto.setContadorReintentos(tarea.getContadorReintentos());
		dto.setMaxReintentos(tarea.getMaxReintentos());
		dto.setFechaCreacion(tarea.getFechaCreacion());
		dto.setFechaActualizacion(tarea.getFechaActualizacion());
		return dto;
	}

	public ResponseEntity<Object> cancelarTarea(UUID idTarea) {
		if (idTarea == null) {
			return ResponseEntity.badRequest().build();
		}
		Tarea tarea = repositorioTarea.findById(idTarea).orElseThrow(() -> new TareaNoEncontradaException(idTarea));
		if (tarea.getEstado().equals(EstadoTarea.PENDIENTE) || tarea.getEstado().equals(EstadoTarea.PROGRAMADA)
				|| tarea.getEstado().equals(EstadoTarea.ENCOLADA)) {
			tarea.setEstado(EstadoTarea.CANCELADA);
			repositorioTarea.save(tarea);
			return ResponseEntity.ok("La tarea se ha cancelado correctamente");
		} else {
			EstadoTarea estado = tarea.getEstado();
			return ResponseEntity.status(HttpStatus.CONFLICT).body("La tarea esta en estado: " + estado);
		}
	}

}
