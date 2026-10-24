package com.uth.pulperia;

import java.util.Locale;

public class Formato {
    public static String lempiras(double valor) {
        return String.format(Locale.US, "L %,.2f", valor);
    }
}
