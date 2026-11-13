package monedero.utilidades;

public class FormatoCedula {
    public static String normalizar(String id) {
        return id != null && id.matches("[0-9]{1,3}(\\.[0-9]{3})+")
                ? id.replace(".", "") : id;
    }

    public static String mostrar(String id) {
        if (id == null) return "—";
        return normalizar(id).replaceAll("(?<=\\d)(?=(\\d{3})+$)", ".");
    }
}
