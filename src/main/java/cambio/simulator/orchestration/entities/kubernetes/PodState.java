package cambio.simulator.orchestration.entities.kubernetes;

public enum PodState {
    PENDING,
    RUNNING,
    SUCCEEDED,
    TERMINATING,
    FAILED,
    UNKNOWN
}
