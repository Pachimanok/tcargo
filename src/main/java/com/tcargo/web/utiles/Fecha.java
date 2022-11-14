package com.tcargo.web.utiles;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Fecha {
	
    public static SimpleDateFormat FECHA_GUIONES = new SimpleDateFormat("dd-MM-yyyy");

    public static SimpleDateFormat FECHA = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es"));
    public static SimpleDateFormat FECHA_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("es"));
    public static SimpleDateFormat FECHA_HORA = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));
    public static SimpleDateFormat FECHA_CALENDAR = new SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("es"));
    public static SimpleDateFormat FECHA_CALENDAR_CON_HORA = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.forLanguageTag("es"));


    public static SimpleDateFormat FECHA_DIA = new SimpleDateFormat("EEEE dd", Locale.forLanguageTag("es"));

    private static final Fecha instance = new Fecha();

    public static NumberFormat FORMATO_INGLES;
    public static NumberFormat FORMATO_ESPANOL;

    static {
        FORMATO_INGLES = NumberFormat.getInstance(Locale.US);
        FORMATO_INGLES.setMinimumFractionDigits(2);
        FORMATO_INGLES.setMaximumFractionDigits(2);
        FORMATO_INGLES.setGroupingUsed(false);

        FORMATO_ESPANOL = NumberFormat.getInstance(Locale.US);
        FORMATO_ESPANOL.setMinimumFractionDigits(2);
        FORMATO_ESPANOL.setMaximumFractionDigits(2);
        FORMATO_ESPANOL.setGroupingUsed(true);
    }
    
    private Fecha() {
    	
    }

    public static Fecha getInstance() {
        return instance;
    }

    public static String formatearFecha(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    public static String formatFecha(Date f) {
		try {
			return FECHA.format(f);
		} catch (Exception e) {
			return null;
		}
	}

    public static String formatearFechaGuiones(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static Date obtenerFecha(String fecha) {
        return Date.from(LocalDate.parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
