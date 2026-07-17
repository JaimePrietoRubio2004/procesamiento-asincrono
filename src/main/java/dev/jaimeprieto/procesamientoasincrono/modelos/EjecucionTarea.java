package dev.jaimeprieto.procesamientoasincrono.modelos;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ejecucion_tarea")
public class EjecucionTarea {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id")
	private UUID id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tarea_id", nullable = false)
	private Tarea tarea;
	
	@Column(name = "numero_intentos", nullable = false)
	private int numeroIntento;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoEjecucion estado;
	
	@Column(name = "mensaje_error", columnDefinition = "TEXT" ,nullable = true)
	private String mensajeError;
	
	@Column(name = "fecha_inicio", nullable = false)
	private LocalDateTime fechaInicio;
	
	@Column(name = "fecha_fin", nullable = true)
	private LocalDateTime fechaFin;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Tarea getTarea() {
		return tarea;
	}

	public void setTarea(Tarea tarea) {
		this.tarea = tarea;
	}

	public int getNumeroIntento() {
		return numeroIntento;
	}

	public void setNumeroIntento(int numeroIntento) {
		this.numeroIntento = numeroIntento;
	}

	public EstadoEjecucion getEstado() {
		return estado;
	}

	public void setEstado(EstadoEjecucion estado) {
		this.estado = estado;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDateTime fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDateTime fechaFin) {
		this.fechaFin = fechaFin;
	}

	public EjecucionTarea(UUID id, Tarea tarea, int numeroIntento, EstadoEjecucion estado, String mensajeError,
			LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		super();
		this.id = id;
		this.tarea = tarea;
		this.numeroIntento = numeroIntento;
		this.estado = estado;
		this.mensajeError = mensajeError;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public EjecucionTarea() {
		super();
	}
	
	
}
