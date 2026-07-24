package dev.jaimeprieto.procesamientoasincrono.dto;

import java.time.LocalDateTime;

import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoEjecucion;

public class EjecucionTareaDto {

	private int numeroIntento;

	private EstadoEjecucion estado;

	private String mensajeError;

	private LocalDateTime fechaInicio;

	private LocalDateTime fechaFin;

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

}
