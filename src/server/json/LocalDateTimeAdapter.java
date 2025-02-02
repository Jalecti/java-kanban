package server.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        String toWrite = "null";
        if (localDateTime != null) toWrite = localDateTime.format(FORMATTER);
        jsonWriter.value(toWrite);
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String nextString = jsonReader.nextString();
        if (nextString.equals("null")) return null;
        return LocalDateTime.parse(nextString);
    }
}
