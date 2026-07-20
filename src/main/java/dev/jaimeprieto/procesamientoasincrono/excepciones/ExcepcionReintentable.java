package dev.jaimeprieto.procesamientoasincrono.excepciones;

public class ExcepcionReintentable extends RuntimeException {

	public ExcepcionReintentable(String mensaje) {
		super(mensaje);
	}

	public ExcepcionReintentable(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

}
