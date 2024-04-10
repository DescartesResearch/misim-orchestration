package cambio.simulator.orchestration.models;

import cambio.simulator.events.ExperimentAction;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.events.OrchestrationExperimentAction;
import cambio.simulator.parsing.EntityCreator;
import cambio.simulator.parsing.JsonTypeNameResolver;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static cambio.simulator.parsing.adapter.experiment.ExperimentModelAdapter.CURRENT_JSON_OBJECT_NAME_KEY;

/**
 * Adapter for parsing {@link ExperimentAction} objects from JSON. This adapter is necessary to parse custom
 * orchestration
 * events like ChaosMonkeyForPodsEvent.
 */
public class OrchestrationExperimentActionAdapter extends MiSimModelReferencingTypeAdapter<ExperimentAction> {

    private Gson parser;

    public OrchestrationExperimentActionAdapter(MiSimModel model) {
        super(model);
    }


    public void setParser(Gson parser) {
        this.parser = parser;
    }

    @Override
    public void write(JsonWriter out, ExperimentAction value) throws IOException {

    }

    @Override
    public ExperimentAction read(JsonReader in) throws IOException {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
        jsonObject.isJsonArray();

        TypeNameAssociatedConfigurationData configData = parser.fromJson(jsonObject,
                TypeNameAssociatedConfigurationData.class);

        String concreteTypeJsonName = extractConcreteTypeName(jsonObject);

        Class<? extends ExperimentAction> concreteTargetClass =
                JsonTypeNameResolver.resolveFromJsonTypeName(concreteTypeJsonName, ExperimentAction.class);
        if (concreteTargetClass == null) {
            concreteTargetClass = JsonTypeNameResolver.resolveFromJsonTypeName(concreteTypeJsonName,
                    OrchestrationExperimentAction.class);
            // throw new ParsingException(String.format("Could not find type '%s' as subtype of '%s'",
            //          concreteTypeJsonName, ExperimentAction.class.getName()));
        }

        String experimentActionName = extractExperimentActionName(jsonObject, concreteTargetClass);

        EntityCreator<? extends ExperimentAction> creator = EntityCreator.getCreator(model, experimentActionName,
                concreteTargetClass);
        Gson newGson =
                parser.newBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(concreteTargetClass,
                        creator).create();
        return newGson.fromJson(configData.getConfigAsJsonString(), concreteTargetClass);
    }

    private String extractExperimentActionName(@NotNull JsonObject jsonObject,
                                               Class<? extends ExperimentAction> concreteTargetClass) {
        String experimentActionName;
        experimentActionName = jsonObject.has(CURRENT_JSON_OBJECT_NAME_KEY) ?
                jsonObject.get(CURRENT_JSON_OBJECT_NAME_KEY).getAsString() : concreteTargetClass.getSimpleName();
        JsonObject config = jsonObject.getAsJsonObject("config");
        if (config != null && config.has(CURRENT_JSON_OBJECT_NAME_KEY)) {
            experimentActionName = config.get(CURRENT_JSON_OBJECT_NAME_KEY).getAsString();
        }
        return experimentActionName;
    }

    private String extractConcreteTypeName(@NotNull JsonObject jsonObject) {
        if (jsonObject.has("type")) {
            return jsonObject.get("type").getAsString();
        } else {
            throw new ParsingException("Experiment action is missing a 'type' argument.");
        }
    }
}
