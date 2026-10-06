package py.tortas3b.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilidades de formato para la interfaz y los reportes:
 * Guaraníes paraguayos (Gs.) con separadores de miles y sin decimales,
 * fechas en dd/MM/yyyy y horas en HH:mm.
 */
public final class FormatoUtil {

    private static final DecimalFormat FORMATO_GS;
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "PY"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        FORMATO_GS = new DecimalFormat("#,##0", symbols);
    }

    private FormatoUtil() {}

    public static String guaranies(BigDecimal monto) {
        if (monto == null) return "0 Gs.";
        return FORMATO_GS.format(monto) + " Gs.";
    }

    public static String guaranies(long monto) {
        return guaranies(BigDecimal.valueOf(monto));
    }

    public static String fecha(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(FECHA_FMT);
    }

    public static String hora(LocalTime hora) {
        if (hora == null) return "";
        return hora.format(HORA_FMT);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) return "";
        return fechaHora.format(FECHA_HORA_FMT);
    }

    public static String kilos(BigDecimal kg) {
        if (kg == null) return "0,000 kg";
        return String.format(Locale.GERMANY, "%.3f kg", kg.doubleValue());
    }
}
