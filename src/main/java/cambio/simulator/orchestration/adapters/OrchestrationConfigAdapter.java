package cambio.simulator.orchestration.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;

public class OrchestrationConfigAdapter extends TypeAdapter<OrchestrationConfig> {
    private final File location;

    public OrchestrationConfigAdapter(File location) {
        this.location = location;
    }

    @Override
    public void write(JsonWriter out, OrchestrationConfig value) throws IOException {

    }

    @Override
    public OrchestrationConfig read(JsonReader in) throws IOException {
        JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
        if (!root.has("orchestrate")) {
            throw new JsonParseException("Orchestration config must have field orchestrate (type: boolean)");
        }
        boolean orchestrate = root.getAsJsonPrimitive("orchestrate").getAsBoolean();
        if (!root.has("orchestration_dir")) {
            throw new JsonParseException("Orchestration config must have field orchestrate (type: String)");
        }
        String orchestrationDir = root.getAsJsonPrimitive("orchestration_dir").getAsString();
        OrchestrationConfig config = new OrchestrationConfig();
        config.setOrchestrated(orchestrate);
        config.setOrchestrationDirectory(orchestrationDir);
        return config;
    }
}
