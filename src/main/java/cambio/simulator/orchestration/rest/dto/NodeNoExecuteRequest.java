package cambio.simulator.orchestration.rest.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeNoExecuteRequest {

    List<String> nodes;

    public NodeNoExecuteRequest() {
    }
}
