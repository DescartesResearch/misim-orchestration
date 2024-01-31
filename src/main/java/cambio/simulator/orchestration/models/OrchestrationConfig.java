package cambio.simulator.orchestration.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OrchestrationConfig {

    private boolean orchestrate;
    private String orchestrationDir;
    private boolean importNodes;
    private Nodes nodes;
    private NetworkDelays networkDelays;
    private Scaler scaler;
    private String scheduler;
    private InitialSchedulingOrder initialSchedulingOrder;
    private int scalingInterval;
    private int healthCheckDelay;
    private List<CustomNodes> customNodes;
    private List<SchedulerPrio> schedulerPrio;
    private List<StartUpTimeContainer> startUpTimeContainer;
    private boolean useClusterAutoscaler;

    public OrchestrationConfig() {
        importNodes = false;
        scaler = new Scaler();
        scalingInterval = 15;
        healthCheckDelay = 0;
    }

    @Getter
    @Setter
    public static class InitialSchedulingOrder {
        private boolean enabled;
        private List<String> order;
    }

    @Getter
    @Setter
    public static class NetworkDelays {
        boolean enabled;
        Map<String, Map<String, NetworkInfo>> delayMap;

        @Getter
        @Setter
        public static class NetworkInfo {
            double mean;
            double std;
        }
    }

    @Getter
    @Setter
    public static class CustomNodes {
        String name;
        int cpu;
    }

    @Getter
    @Setter
    public static class SchedulerPrio {
        String name;
        int prio;
    }

    @Getter
    @Setter
    public static class StartUpTimeContainer {
        String name;
        int time;
    }

    @Getter
    @Setter
    public static class Nodes {
        int amount;
        int cpu;
    }

    @Getter
    @Setter
    public static class Scaler {

        boolean importScaler;
        List<ScalerSpec> scalerList;

        public Scaler() {
            importScaler = false;
            scalerList = new ArrayList<>();
        }
    }

    @Getter
    @Setter
    public static class ScalerSpec {
        String service;
        String scalerType;
        double targetUtilization;
        int minReplicas;
        int maxReplicas;
        double lowerBound;
        double upperBound;
        int holdTime;
        int increment;
        int decrement;
    }

}
