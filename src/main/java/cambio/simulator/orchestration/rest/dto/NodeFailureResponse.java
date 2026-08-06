
package cambio.simulator.orchestration.rest.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeFailureResponse {

    // The duration (in seconds) after which the failed nodes will be marked
    // "NotReady".
    int nodeMonitorGracePeriodSeconds;
    // The duration (in seconds) after which a "NotReady" node will also be tainted
    // with a "NoExecute" taint.
    int noExecuteTaintDelaySeconds;
    // A map of delays (in seconds) after a node is marked "NotReady" to the list of
    // pods that should be evicted at that time.
    Map<Long, List<String>> podEvictionEvents;

    public NodeFailureResponse() {
    }

}
