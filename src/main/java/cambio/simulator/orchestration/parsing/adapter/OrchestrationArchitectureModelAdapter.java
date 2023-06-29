package cambio.simulator.orchestration.parsing.adapter;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.networking.SimpleDependencyDescription;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import cambio.simulator.parsing.adapter.architecture.ArchitectureModelAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import desmoj.core.dist.ContDistNormal;

public class OrchestrationArchitectureModelAdapter extends ArchitectureModelAdapter {
    public OrchestrationArchitectureModelAdapter(MiSimOrchestrationModel miSimOrchestrationModel) {
        super(miSimOrchestrationModel);
    }

    @Override
    public ArchitectureModel read(JsonReader in) {
        JsonObject root = JsonParser.parseReader(in).getAsJsonObject();

        Gson gson = GsonHelper
                .getGsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(model))
                .registerTypeAdapter(Microservice.class, new MicroserviceOrchestrationAdapter(model, dependencies))
                .create();

        ArchitectureModel architectureModel = gson.fromJson(root, ArchitectureModel.class);

        for (DependencyDescription dependency : dependencies) {
            ((SimpleDependencyDescription) dependency).resolveNames(architectureModel);
        }
        return architectureModel;
    }
}
