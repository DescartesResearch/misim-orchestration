package cambio.simulator.orchestration.rest.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeNotReadyRequest {
    List<String> nodes;

    public NodeNotReadyRequest() {
    }
}
