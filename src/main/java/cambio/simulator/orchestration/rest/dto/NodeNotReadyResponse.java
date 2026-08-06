package cambio.simulator.orchestration.rest.dto;

import java.util.List;

import cambio.simulator.orchestration.parsing.kubernetes.KubernetesObjectWithMetadataSpec;
import io.kubernetes.client.openapi.models.V1Node;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeNotReadyResponse {
    List<V1Node> nodes;
    List<KubernetesObjectWithMetadataSpec> machines;
    List<KubernetesObjectWithMetadataSpec> machineSets;

    public NodeNotReadyResponse() {
    }
}
