package cambio.simulator.orchestration.parsing;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.orchestration.models.OrchestrationConfig;
import cambio.simulator.orchestration.parsing.adapter.OrchestrationArchitectureModelAdapter;
import cambio.simulator.orchestration.parsing.adapter.OrchestrationConfigAdapter;
import cambio.simulator.parsing.ModelLoader;

import java.io.File;

public class OrchestrationModelLoader {
    public static OrchestrationConfig loadOrchestrationConfig(File location) {
        return ModelLoader.loadModel(location,
                OrchestrationConfig.class,
                new OrchestrationConfigAdapter(location));
    }

    public static ArchitectureModel loadArchitectureModel(MiSimOrchestrationModel baseModel) {
        return ModelLoader.loadModel(
                baseModel.getExperimentMetaData().getArchitectureDescriptionLocation(),
                ArchitectureModel.class,
                new OrchestrationArchitectureModelAdapter(baseModel)
        );
    }
}
