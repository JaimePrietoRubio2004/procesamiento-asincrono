package dev.jaimeprieto.procesamientoasincrono.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.PrioridadTarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TareaDto {

	private UUID idTarea;
	
	@NotBlank
	private String tipo;
	
	@NotNull
	private Map<String, Object> payload;

	private EstadoTarea estado;
	
	private PrioridadTarea prioridad;

	private int contadorReintentos;

	private Integer maxReintentos;

	private LocalDateTime fechaProgramada;

	private String expresionCron;

	private String mensajeError;

	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaActualizacion;

	private LocalDateTime fechaFinalizacion;

	public UUID getIdTarea() {
		return idTarea;
	}

	public void setIdTarea(UUID idTarea) {
		this.idTarea = idTarea;
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

	public Integer getMaxReintentos() {
		return maxReintentos;
	}

	public void setMaxReintentos(Integer maxReintentos) {
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

	public TareaDto(UUID idTarea, String tipo, Map<String, Object> payload, EstadoTarea estado, PrioridadTarea prioridad,
			int contadorReintentos, Integer maxReintentos, LocalDateTime fechaProgramada, String expresionCron,
			String mensajeError, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
			LocalDateTime fechaFinalizacion) {
		super();
		this.idTarea = idTarea;
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

	public TareaDto() {
		super();
	}
}
