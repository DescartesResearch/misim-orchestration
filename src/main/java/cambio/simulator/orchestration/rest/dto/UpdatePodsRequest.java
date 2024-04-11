package cambio.simulator.orchestration.rest.dto;

import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1WatchEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class UpdatePodsRequest {
    private V1PodList allPods;
    private List<V1WatchEvent> events;
    private V1PodList podsToBePlaced;

    public UpdatePodsRequest() {
    }
}
