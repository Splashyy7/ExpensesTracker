package org.example.persistence;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Adaptador Gson para serialização de {@link LocalDate}.
 */
class LocalDateAdapter extends TypeAdapter<LocalDate> {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(FORMATO.format(value));
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        String texto = in.nextString();
        return LocalDate.parse(texto, FORMATO);
    }
}
