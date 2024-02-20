package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.export.Stats;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

public class StatsEvent extends NamedExternalEvent {
    public StatsEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        Stats.getInstance().createScalingStats(getModel());
        Stats.getInstance().createSchedulingStats(getModel());
        System.out.println(presentTime());
    }


}
