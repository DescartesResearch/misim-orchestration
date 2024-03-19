package cambio.simulator.orchestration.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import io.kubernetes.client.openapi.models.EventsV1EventList;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class EventsController {
    private static final String API_URL = "http://127.0.0.1:8000/events";

    public static EventsV1EventList getEvents() throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
        con.setRequestMethod("GET");

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get events: HTTP error code: " + con.getResponseCode());
        }
        // Gson doesn't know how to parse java time types, so we register a custom type adapter to correctly parse
        // eventTime fields
        Gson gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class,
                (JsonDeserializer<OffsetDateTime>) (json, typeOfT, context) -> OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)).create();

        try (InputStreamReader reader = new InputStreamReader(con.getInputStream())) {
            return gson.fromJson(reader, EventsV1EventList.class);
        } finally {
            con.disconnect();
        }
    }
}
