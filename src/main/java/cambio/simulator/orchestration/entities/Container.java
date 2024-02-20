package cambio.simulator.orchestration.entities;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.events.HealthCheckEvent;
import cambio.simulator.orchestration.events.TryToRestartContainerEvent;
import cambio.simulator.orchestration.events.StartContainerAndMicroserviceInstanceEvent;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.entities.kubernetes.PodState;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import lombok.Getter;
import lombok.Setter;

/**
 * Basically represents a 1:1-relationsship to @link{MicroserviceInstance} with a coupled @link{ContainerState}
 */
@Getter
@Setter
public class Container extends NamedEntity {
    private MicroserviceInstance microserviceInstance;
    private ContainerState containerState;

    private int restartAttemptsLeft = 0;
    private int backOffDelay = 10;
    private TimeInstant lastRetry = null;

    public Container(Model model, String name, boolean showInTrace, MicroserviceInstance microserviceInstance) {
        super(model, name, showInTrace);
        this.microserviceInstance = microserviceInstance;
        this.containerState = ContainerState.WAITING;
    }

    public void start() {
        StartContainerAndMicroserviceInstanceEvent startMicroServiceEvent = new StartContainerAndMicroserviceInstanceEvent(getModel(), "StartContainerEvent", traceIsOn());
        if (microserviceInstance != null) startMicroServiceEvent.schedule(this, new TimeSpan(((MicroserviceOrchestration) microserviceInstance.getOwner()).getStartTime()));
        else startMicroServiceEvent.schedule(this, new TimeSpan(0));
    }

    //Restart terminated container regarding restart policy https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
    public void restart(){
        applyBackOffDelayResetIfNecessary();
        final TryToRestartContainerEvent tryToRestartContainerEvent = new TryToRestartContainerEvent(getModel(), "Restart " + getQuotedPlainName(), traceIsOn());
        tryToRestartContainerEvent.schedule(this, new TimeSpan(getBackOffDelay()));
    }

    public void die() {
        MicroserviceInstance instanceToKill = microserviceInstance;
        if (instanceToKill != null) {
            instanceToKill.die();
            instanceToKill.getOwner().getInstancesSet().remove(instanceToKill);
        }

        Pod pod = ManagementPlane.getInstance().getPodForContainer(this);
        if (pod != null) {

            setContainerState(ContainerState.TERMINATED);

            long count = pod.getContainers().stream().filter(container1 -> container1.getContainerState().equals(ContainerState.RUNNING)).count();
            //If no container is running inside this pod, then mark this pod as FAILED
            if (count == 0) {
                pod.setPodState(PodState.FAILED);
                sendTraceNote("Pod " + pod.getQuotedName() + " was set to FAILED because it has not a single running container inside");
                HealthCheckEvent healthCheckEvent = new HealthCheckEvent(getModel(), "HealthCheckEvent - After Pod failed", traceIsOn());
                healthCheckEvent.schedule(new TimeSpan(HealthCheckEvent.delay));

            } else {
                //Restart terminated container regarding restart policy https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
                restart();
            }
        } else {
            throw new IllegalStateException("Pod should never be null. When a container dies it must have been in a pod before.");
        }
    }

    public void incrementBackOffDelay() {
        int LIMIT_BACK_OFF_DELAY = 300;
        if (backOffDelay == LIMIT_BACK_OFF_DELAY) {
            return;
        }
        backOffDelay = Math.min(backOffDelay * 2, LIMIT_BACK_OFF_DELAY);
    }

    public void resetBackOffDelay() {
        backOffDelay = 10;
    }


    public void applyBackOffDelayResetIfNecessary() {
        if (lastRetry != null) {
            final double timeAsDouble = presentTime().getTimeAsDouble();
            int RESET_BACK_OFF_DELAY_AFTER_TIME = 600;
            if (timeAsDouble - lastRetry.getTimeAsDouble() > RESET_BACK_OFF_DELAY_AFTER_TIME) {
                resetBackOffDelay();
            }
        }
    }

    public boolean canRestartOtherwiseDecrease(){
        if(restartAttemptsLeft>0){
            restartAttemptsLeft--;
            return false;
        } else {
            return true;
        }
    }
}
