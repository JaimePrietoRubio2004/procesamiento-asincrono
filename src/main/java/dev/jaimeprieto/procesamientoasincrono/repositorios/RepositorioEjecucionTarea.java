package dev.jaimeprieto.procesamientoasincrono.repositorios;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jaimeprieto.procesamientoasincrono.modelos.EjecucionTarea;

public interface RepositorioEjecucionTarea extends JpaRepository<EjecucionTarea, UUID> {

	List<EjecucionTarea> findByTareaIdOrderByNumeroIntentoAsc(UUID tareaId);
}
