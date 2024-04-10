package cambio.simulator.orchestration.events;

import cambio.simulator.events.ISelfScheduled;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.Objects;

/**
 * Base class for all self-scheduled experiment actions in the orchestration domain. This is necessary to incldue
 */
public abstract class OrchestrationSelfScheduledExperimentAction extends OrchestrationExperimentAction implements ISelfScheduled {

    public OrchestrationSelfScheduledExperimentAction(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void doInitialSelfSchedule() {
        this.schedule(new TimeInstant(initialArrivalTime));
    }

    public void setTargetTime(TimeInstant targetTime) {
        Objects.requireNonNull(targetTime);
        this.initialArrivalTime = targetTime.getTimeAsDouble();
    }

}
