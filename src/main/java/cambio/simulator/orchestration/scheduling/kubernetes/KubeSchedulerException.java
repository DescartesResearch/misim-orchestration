package cambio.simulator.orchestration.scheduling.kubernetes;

public class KubeSchedulerException extends Exception {

    public KubeSchedulerException(String errorMessage){
        super(errorMessage);
    }
}