package cambio.simulator.orchestration.management;

import cambio.simulator.orchestration.events.HealthCheckEvent;
import cambio.simulator.orchestration.models.OrchestrationConfig;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultValues {
    @Getter
    @Setter
    String scheduler;

    @Getter
    private static final DefaultValues instance = new DefaultValues();

    //private constructor to avoid client applications to use constructor
    private DefaultValues() {
    }

    public void setDefaultValuesFromConfigFile(OrchestrationConfig configDto) throws ParsingException {
        final SchedulerType schedulerType = SchedulerType.fromString(configDto.getScheduler());
        if (schedulerType != null) {
            scheduler = schedulerType.getName();
        } else {
            final List<String> possibleValues = Arrays.stream(SchedulerType.values()).map(SchedulerType::getName).collect(Collectors.toList());
            throw new ParsingException("Unknown SchedulerType in config file: " + configDto.getScheduler() + "\nPossible values are: " + possibleValues);
        }
        HealthCheckEvent.delay = configDto.getHealthCheckDelay();
    }
}
