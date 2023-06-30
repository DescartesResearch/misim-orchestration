package cambio.simulator.orchestration.scaling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.util.Random;

public abstract class AutoScaler extends NamedEntity {

    public AutoScaler() {
        super(ManagementPlane.getInstance().getModel(), "AutoScaler", ManagementPlane.getInstance().getModel().traceIsOn());
    }

    public abstract void apply(Deployment deployment);

}
