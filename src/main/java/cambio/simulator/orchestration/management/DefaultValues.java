package cambio.simulator.orchestration.management;

import cambio.simulator.orchestration.events.HealthCheckEvent;
import cambio.simulator.orchestration.models.OrchestrationConfig;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.orchestration.scheduling.SchedulerType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultValues {
    String scheduler;

    private static final DefaultValues instance = new DefaultValues();

    //private constructor to avoid client applications to use constructor
    private DefaultValues() {
    }

    public static DefaultValues getInstance() {
        return instance;
    }

    public void setDefaultValuesFromConfigFile(OrchestrationConfig configDto) throws ParsingException {
        final SchedulerType schedulerType = SchedulerType.fromString(configDto.getScheduler());
        if (schedulerType != null) {
            scheduler = schedulerType.getName();
        } else {
            final List<String> possibleValues = Arrays.stream(SchedulerType.values()).map(schedulerType1 -> schedulerType1.getName()).collect(Collectors.toList());
            throw new ParsingException("Unknown SchedulerType in config file: " + configDto.getScheduler() + "\nPossible values are: " + possibleValues);
        }
        if (configDto.getHealthCheckDelay()!=null){
            HealthCheckEvent.delay = Integer.valueOf(configDto.getHealthCheckDelay());
        }
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }
}
