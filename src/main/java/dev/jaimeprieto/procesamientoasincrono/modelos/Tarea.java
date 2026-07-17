package dev.jaimeprieto.procesamientoasincrono.modelos;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tarea")
public class Tarea {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id")
	private UUID id;

	@Column(name = "tipo", length = 100, nullable = false)
	private String tipo;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload", columnDefinition = "jsonb", nullable = false)
	private Map<String, Object> payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoTarea estado;

	@Enumerated(EnumType.STRING)
	@Column(name = "prioridad", nullable = false)
	private PrioridadTarea prioridad;

	@Column(name = "contador_reintentos", nullable = false)
	private int contadorReintentos;

	@Column(name = "max_reintentos", nullable = false)
	private int maxReintentos;

	@Column(name = "fecha_programada", nullable = true)
	private LocalDateTime fechaProgramada;

	@Column(name = "expresion_cron", length = 100, nullable = true)
	private String expresionCron;

	@Column(name = "mensaje_error", columnDefinition = "TEXT", nullable = true)
	private String mensajeError;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_actualizacion", nullable = false)
	private LocalDateTime fechaActualizacion;

	@Column(name = "fecha_finalizacion", nullable = true)
	private LocalDateTime fechaFinalizacion;

	public UUID getId() {
		return id;
	}

	public Tarea(UUID id, String tipo, Map<String, Object> payload, EstadoTarea estado, PrioridadTarea prioridad,
			int contadorReintentos, int maxReintentos, LocalDateTime fechaProgramada, String expresionCron,
			String mensajeError, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
			LocalDateTime fechaFinalizacion) {
		super();
		this.id = id;
		this.tipo = tipo;
		this.payload = payload;
		this.estado = estado;
		this.prioridad = prioridad;
		this.contadorReintentos = contadorReintentos;
		this.maxReintentos = maxReintentos;
		this.fechaProgramada = fechaProgramada;
		this.expresionCron = expresionCron;
		this.mensajeError = mensajeError;
		this.fechaCreacion = fechaCreacion;
		this.fechaActualizacion = fechaActualizacion;
		this.fechaFinalizacion = fechaFinalizacion;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}

	public EstadoTarea getEstado() {
		return estado;
	}

	public void setEstado(EstadoTarea estado) {
		this.estado = estado;
	}

	public PrioridadTarea getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(PrioridadTarea prioridad) {
		this.prioridad = prioridad;
	}

	public int getContadorReintentos() {
		return contadorReintentos;
	}

	public void setContadorReintentos(int contadorReintentos) {
		this.contadorReintentos = contadorReintentos;
	}

	public int getMaxReintentos() {
		return maxReintentos;
	}

	public void setMaxReintentos(int maxReintentos) {
		this.maxReintentos = maxReintentos;
	}

	public LocalDateTime getFechaProgramada() {
		return fechaProgramada;
	}

	public void setFechaProgramada(LocalDateTime fechaProgramada) {
		this.fechaProgramada = fechaProgramada;
	}

	public String getExpresionCron() {
		return expresionCron;
	}

	public void setExpresionCron(String expresionCron) {
		this.expresionCron = expresionCron;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public LocalDateTime getFechaFinalizacion() {
		return fechaFinalizacion;
	}

	public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
		this.fechaFinalizacion = fechaFinalizacion;
	}

	public Tarea() {
		super();
	}

}
