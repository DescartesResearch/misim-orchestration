package cambio.simulator.orchestration.entities;

import cambio.simulator.entities.microservice.InstanceShutdownEndEvent;
import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.InternalRequest;
import cambio.simulator.entities.networking.Request;
import cambio.simulator.entities.networking.RequestAnswer;
import cambio.simulator.entities.networking.ServiceDependencyInstance;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.resources.cpu.CPUProcess;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class MicroserviceOrchestrationInstance extends MicroserviceInstance {
    public MicroserviceOrchestrationInstance(Model model, String name, boolean showInTrace, Microservice microservice,
                                int instanceID) {
        super(model, name, showInTrace, microservice, instanceID);
        if (!(model instanceof MiSimOrchestrationModel)) {
            throw new IllegalArgumentException("model must be of type MiSimOrchestrationModel");
        }
    }

    @Override
    protected void submitProcessToCPU(CPUProcess newProcess) {
        MiSimOrchestrationModel model = (MiSimOrchestrationModel) getModel();
        if (model.getOrchestrationConfig().isOrchestrate()){
            MicroserviceOrchestration owner = (MicroserviceOrchestration) this.getOwner();
            MicroserviceInstance nextAvailableInstance = owner.getNextAvailableInstance();
            nextAvailableInstance.getCpu().submitProcess(newProcess);
        } else {
            super.submitProcessToCPU(newProcess);
        }
    }

    @Override
    protected void handleIncomingRequest(Request request) {
        if (currentRequestsToHandle.add(request)) { //register request and stamp as received if not already known
            request.setHandler(this);
            notComputed++;
            waiting++;
        }

        //three possibilities:
        //1. request is completed -> send it back to its sender (target is retrieved by the SendEvent)
        //2. requests' dependencies were all received -> send it to the cpu for handling.
        //   The CPU will "send" it back to this method once it is done.
        //3. request does have dependencies -> create internal request
        if (request.isCompleted()) {
            notComputed--;
            RequestAnswer answer = new RequestAnswer(request, this);
            // We enforce the cluster network delay (round trip time) when sending the answer because only here a know
            // the sending instance (this object) and the receiving instance (the requester)
            // when we would do it when sending the other dependencies we dont know the receiving instance
            double delay = getNetworkDelay(this, request.getRequester());
            if (delay > 0) {
                sendRequest("Request_Answer_" + request.getPlainName(), answer, request.getRequester(), new TimeSpan(delay));
            } else {
                sendRequest("Request_Answer_" + request.getPlainName(), answer, request.getRequester());
            }

            int size = currentRequestsToHandle.size();
            currentRequestsToHandle.remove(request);
            assert currentRequestsToHandle.size() == size - 1;

            //shutdown after the last answer was send. It doesn't care if the original sender does not live anymore
            if (currentRequestsToHandle.isEmpty() && getState() == InstanceState.SHUTTING_DOWN) {
                InstanceShutdownEndEvent event = new InstanceShutdownEndEvent(getModel(),
                        String.format("Instance %s Shutdown End", this.getQuotedName()), traceIsOn());
                event.schedule(this, presentTime());
            }

        } else if (request.getDependencies().isEmpty() || request.areDependenciesCompleted()) {
            waiting--;
            CPUProcess newProcess = new CPUProcess(request);
            submitProcessToCPU(newProcess);
        } else {
            for (ServiceDependencyInstance dependency : request.getDependencies()) {
                currentlyOpenDependencies.add(dependency);

                Request internalRequest = new InternalRequest(getModel(), this.traceIsOn(), dependency, this);
                sendRequest(String.format("Collecting dependency %s", dependency.getQuotedName()), internalRequest,
                        dependency.getTargetService());
                sendTraceNote(String.format("Try 1, send Request: %s ", internalRequest.getQuotedPlainName()));
            }
        }
    }

    private double getNetworkDelay(MicroserviceInstance source, MicroserviceInstance target) {
        Pod src = ManagementPlane.getInstance().getPodForContainer(ManagementPlane.getInstance().getContainerForMicroServiceInstance(source));
        Pod tar = ManagementPlane.getInstance().getPodForContainer(ManagementPlane.getInstance().getContainerForMicroServiceInstance(target));
        if (tar != null) {
            double delay = ManagementPlane.getInstance().getCluster().getNetworkDelay(src.getLastKnownNode().getPlainName(), tar.getLastKnownNode().getPlainName());
            System.out.printf("Adding delay %f between %s (Node: %s) and %s (Node: %s)\n", delay, src.getPlainName(), src.getLastKnownNode().getPlainName(), tar.getPlainName(), tar.getLastKnownNode().getPlainName());
            return delay;
        } else {
            return 0;
        }
    }
}
