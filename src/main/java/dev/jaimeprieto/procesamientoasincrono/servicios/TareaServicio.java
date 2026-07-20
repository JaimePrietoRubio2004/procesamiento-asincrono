package dev.jaimeprieto.procesamientoasincrono.servicios;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

	private static final String TAREAS_INTERCAMBIO = "tareas.intercambio";

	private static final String TAREAS_BAJA = "tareas.baja";

	private static final String TAREAS_MEDIA = "tareas.media";

	private static final String TAREAS_ALTA = "tareas.alta";

	private final RepositorioTarea repositorioTarea;

	private final RabbitTemplate rabbitTemplate;

	public TareaServicio(RepositorioTarea repositoriotarea, RabbitTemplate rabbitTemplate) {
		this.repositorioTarea = repositoriotarea;
		this.rabbitTemplate = rabbitTemplate;
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
		tareaGuardada = publicarMensajeYCambioEstado(tareaGuardada);
		TareaDto nuevaTareaDto = new TareaDto();
		nuevaTareaDto.setIdTarea(tareaGuardada.getId());
		nuevaTareaDto.setEstado(tareaGuardada.getEstado());
		nuevaTareaDto.setFechaCreacion(tareaGuardada.getFechaCreacion());
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(nuevaTareaDto);
	}

	private Tarea publicarMensajeYCambioEstado(Tarea tareaGuardada) {
		if (tareaGuardada.getEstado().equals(EstadoTarea.PENDIENTE)) {
			String routingKey = switch (tareaGuardada.getPrioridad()) {
			case ALTA -> TAREAS_ALTA;
			case MEDIA -> TAREAS_MEDIA;
			case BAJA -> TAREAS_BAJA;
			};
			// publica un mensaje en RabbitMQ (internamente convierte un string en un
			// mensaje de AMQP:
			// 1º parametro --> A cual intercambio pertenece
			// 2º parametro --> Con que etiqueta se manda
			// 3º parametro --> El contenido
			rabbitTemplate.convertAndSend(TAREAS_INTERCAMBIO, routingKey, tareaGuardada.getId().toString());
			tareaGuardada.setEstado(EstadoTarea.ENCOLADA);
			tareaGuardada = repositorioTarea.save(tareaGuardada);
		}
		return tareaGuardada;
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

	public ResponseEntity<Object> reintentoManual(UUID idTarea) {
		return reprocesarTareaFallida(idTarea);
	}

	private ResponseEntity<Object> reprocesarTareaFallida(UUID idTarea) {
		if (idTarea == null) {
			return ResponseEntity.badRequest().build();
		}
		Tarea tarea = repositorioTarea.findById(idTarea).orElseThrow(() -> new TareaNoEncontradaException(idTarea));
		if (tarea.getEstado() != EstadoTarea.FALLIDA) {
			EstadoTarea estado = tarea.getEstado();
			return ResponseEntity.status(HttpStatus.CONFLICT).body("La tarea esta en estado: " + estado);
		}
		tarea.setContadorReintentos(0);
		tarea.setMensajeError(null);
		tarea.setEstado(EstadoTarea.PENDIENTE);
		tarea = repositorioTarea.save(tarea);
		tarea = publicarMensajeYCambioEstado(tarea);
		return ResponseEntity.accepted().build();
	}

	public ResponseEntity<Object> reinyeccionManual(UUID idTarea) {
		return reprocesarTareaFallida(idTarea);
	}

	public ResponseEntity<Object> inspeccionMensajeDlq(Pageable pageable) {
		Page<Tarea> tareasFallidas = repositorioTarea.findByEstado(EstadoTarea.FALLIDA, pageable);
		Page<TareaDto> dtoTareasFallidas = tareasFallidas.map(this::convertirADto);
		return ResponseEntity.ok(dtoTareasFallidas);
	}

}
