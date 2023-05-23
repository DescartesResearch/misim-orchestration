package cambio.simulator.orchestration.scheduling.kubernetes;

import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1WatchEvent;

import java.util.List;

public class UpdatePodsRequest {
    private V1PodList allPods;
    private List<V1WatchEvent> events;
    private V1PodList podsToBePlaced;

    public UpdatePodsRequest() {
    }

    public V1PodList getAllPods() {
        return allPods;
    }

    public void setAllPods(V1PodList allPods) {
        this.allPods = allPods;
    }

    public List<V1WatchEvent> getEvents() {
        return events;
    }

    public void setEvents(List<V1WatchEvent> events) {
        this.events = events;
    }

    public V1PodList getPodsToBePlaced() {
        return podsToBePlaced;
    }

    public void setPodsToBePlaced(V1PodList podsToBePlaced) {
        this.podsToBePlaced = podsToBePlaced;
    }
}
