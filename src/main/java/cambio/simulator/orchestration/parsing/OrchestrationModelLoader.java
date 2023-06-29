package cambio.simulator.orchestration.parsing;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.management.DefaultValues;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.orchestration.models.OrchestrationConfig;
import cambio.simulator.orchestration.parsing.adapter.OrchestrationArchitectureModelAdapter;
import cambio.simulator.parsing.ModelLoader;
import cambio.simulator.parsing.ParsingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class OrchestrationModelLoader {
    public static OrchestrationConfig loadOrchestrationConfig(String location) throws IOException, ParsingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final OrchestrationConfig configDto = mapper.readValue(new File(location), OrchestrationConfig.class);
        //Check values for validity and create default values
        DefaultValues.getInstance().setDefaultValuesFromConfigFile(configDto);
        return configDto;
    }

    public static ArchitectureModel loadArchitectureModel(MiSimOrchestrationModel baseModel) {
        return ModelLoader.loadModel(
                baseModel.getExperimentMetaData().getArchitectureDescriptionLocation(),
                ArchitectureModel.class,
                new OrchestrationArchitectureModelAdapter(baseModel)
        );
    }
}
