package cambio.simulator.orchestration.rest.dto;

import java.util.List;

import io.kubernetes.client.openapi.models.V1Node;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeNoExecuteResponse {
    List<V1Node> nodes;

    public NodeNoExecuteResponse() {
    }
}
