package cambio.simulator.orchestration.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PodFailureRequest {
    String failedPod;

    public PodFailureRequest() {
    }
}
