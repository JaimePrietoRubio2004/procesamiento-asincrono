package dev.jaimeprieto.procesamientoasincrono.excepciones;

public class ExcepcionNoReintentable extends RuntimeException {

	public ExcepcionNoReintentable(String mensaje) {
		super(mensaje);
	}

	public ExcepcionNoReintentable(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

}
