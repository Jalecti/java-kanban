package server.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        String toWrite = "null";
        if (duration != null) {
            toWrite = ("PT" + duration.toMinutes() + "M");
        }
        jsonWriter.value(toWrite);
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String nextString = jsonReader.nextString();
        if (nextString.equals("null")) return null;
        return Duration.parse(nextString);
    }
}
