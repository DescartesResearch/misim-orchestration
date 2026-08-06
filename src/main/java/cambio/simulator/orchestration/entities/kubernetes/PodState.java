package cambio.simulator.orchestration.entities.kubernetes;

public enum PodState {
    PENDING,
    RUNNING,
    SUCCEEDED,
    TERMINATING,
    FAILED,
    // Unknown due to node failure that hasn't been detected yet.
    UNKNOWN
}
