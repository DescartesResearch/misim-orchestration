package cambio.simulator.orchestration;

import cambio.simulator.ExperimentCreator;
import cambio.simulator.ExperimentStartupConfig;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import desmoj.core.simulator.Experiment;

import java.io.File;

public class OrchestrationExperimentCreator extends ExperimentCreator {
    @Override
    public Experiment createSimulationExperiment(ExperimentStartupConfig econfig) {
        if (!(econfig instanceof OrchestrationStartupConfig)) {
            throw new IllegalArgumentException("Config must be of type OrchestrationStartupConfig");
        }
        OrchestrationStartupConfig config = (OrchestrationStartupConfig) econfig;
        String archDescLocation = config.getArchitectureDescLoc();
        String expDescLocation;

        expDescLocation = config
                .getExperimentDescLoc() != null
                ? config.getExperimentDescLoc()
                : config.getScenario();

        File architectureDescription =
                tryGetDescription(archDescLocation, "architecture");

        File experimentDescription =
                tryGetDescription(expDescLocation, "experiment/scenario");

        MiSimModel model = new MiSimOrchestrationModel(architectureDescription, experimentDescription, config.getOrchestrationConfigLocation());
        return setupExperiment(config, model);
    }
}
