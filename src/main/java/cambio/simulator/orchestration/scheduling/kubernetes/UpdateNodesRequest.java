package cambio.simulator.orchestration.scheduling.kubernetes;

import cambio.simulator.orchestration.parsing.kubernetes.KubernetesObjectWithMetadataSpec;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1WatchEvent;

import java.util.List;

public class UpdateNodesRequest {
    private V1NodeList allNodes;
    private List<V1WatchEvent> events;
    private List<KubernetesObjectWithMetadataSpec> machineSets;
    private List<KubernetesObjectWithMetadataSpec> machines;

    public UpdateNodesRequest() {
    }

    public V1NodeList getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(V1NodeList allNodes) {
        this.allNodes = allNodes;
    }

    public List<V1WatchEvent> getEvents() {
        return events;
    }

    public void setEvents(List<V1WatchEvent> events) {
        this.events = events;
    }

    public List<KubernetesObjectWithMetadataSpec> getMachineSets() {
        return machineSets;
    }

    public void setMachineSets(List<KubernetesObjectWithMetadataSpec> machineSets) {
        this.machineSets = machineSets;
    }

    public List<KubernetesObjectWithMetadataSpec> getMachines() {
        return machines;
    }

    public void setMachines(List<KubernetesObjectWithMetadataSpec> machines) {
        this.machines = machines;
    }
}
