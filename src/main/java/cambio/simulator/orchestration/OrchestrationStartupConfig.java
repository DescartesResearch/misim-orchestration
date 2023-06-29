package cambio.simulator.orchestration;

import cambio.simulator.CLIOption;
import cambio.simulator.ExperimentStartupConfig;

public class OrchestrationStartupConfig extends ExperimentStartupConfig {

    @CLIOption(
            longOpt = "orchestration",
            description = "Location of the orchestration config file.",
            hasArg = true,
            required = true)
    private final String orchestrationConfigLocation;

    public OrchestrationStartupConfig(String archDescLoc, String expDescLoc, String scenario,
                                      String reportLocation,
                                      boolean showProgressBar, boolean debug, boolean traces, String orchLoc) {
        super(archDescLoc, expDescLoc, scenario, reportLocation, showProgressBar, debug, traces);
        this.orchestrationConfigLocation = orchLoc;
    }

    public String getOrchestrationConfigLocation() {
        return orchestrationConfigLocation;
    }
}
