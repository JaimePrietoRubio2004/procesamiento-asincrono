package dev.jaimeprieto.procesamientoasincrono.repositorios;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.jaimeprieto.procesamientoasincrono.modelos.EstadoTarea;
import dev.jaimeprieto.procesamientoasincrono.modelos.Tarea;

public interface RepositorioTarea extends JpaRepository<Tarea, UUID> {

	Page<Tarea> findByEstado(EstadoTarea estado, Pageable pageable);
	
	Page<Tarea> findByTipo(String tipo, Pageable pageable);
	
	Page<Tarea> findByEstadoAndTipo(EstadoTarea estado, String tipo, Pageable pageable);
}
