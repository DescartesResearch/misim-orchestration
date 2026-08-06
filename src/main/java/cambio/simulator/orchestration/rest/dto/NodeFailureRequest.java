package cambio.simulator.orchestration.rest.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeFailureRequest {

  List<String> failedPods;

  public NodeFailureRequest() {
  }
}
