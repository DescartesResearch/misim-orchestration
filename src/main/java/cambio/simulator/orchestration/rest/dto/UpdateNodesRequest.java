package cambio.simulator.orchestration.rest.dto;

import cambio.simulator.orchestration.parsing.kubernetes.KubernetesObjectWithMetadataSpec;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1WatchEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateNodesRequest {
    private V1NodeList allNodes;
    private List<V1WatchEvent> events;
    private List<KubernetesObjectWithMetadataSpec> machineSets;
    private List<KubernetesObjectWithMetadataSpec> machines;

    public UpdateNodesRequest() {
    }
}
