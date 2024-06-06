package cambio.simulator.orchestration.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import io.kubernetes.client.openapi.models.CoreV1EventList;
import io.kubernetes.client.openapi.models.EventsV1EventList;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class EventsController {
    private static final String API_URL_EVENTS_API_EVENTS = "http://127.0.0.1:8000/getEventsApiEvents";
    private static final String API_URL_CORE_API_EVENTS = "http://127.0.0.1:8000/getCoreApiEvents";

    public static EventsV1EventList getEvents() throws IOException {
        return getEvents(API_URL_EVENTS_API_EVENTS, EventsV1EventList.class);
    }

    public static CoreV1EventList getCoreApiEvents() throws IOException {
        return getEvents(API_URL_CORE_API_EVENTS, CoreV1EventList.class);
    }

    private static <T> T getEvents(String apiUrl, Class<T> eventListClass) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
        con.setRequestMethod("GET");

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get events: HTTP error code: " + con.getResponseCode());
        }

        // Gson doesn't know how to parse java time types, so we register a custom type adapter to correctly parse
        // eventTime fields
        Gson gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class,
                (JsonDeserializer<OffsetDateTime>) (json, typeOfT, context) -> OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)).create();

        try (InputStreamReader reader = new InputStreamReader(con.getInputStream())) {
            return gson.fromJson(reader, eventListClass);
        } finally {
            con.disconnect();
        }
    }
}