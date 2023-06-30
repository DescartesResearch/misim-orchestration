package cambio.simulator.orchestration.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public boolean isOrchestrate() {
        return orchestrate;
    }

    public void setOrchestrate(boolean orchestrate) {
        this.orchestrate = orchestrate;
    }

    public String getOrchestrationDir() {
        return orchestrationDir;
    }

    public void setOrchestrationDir(String orchestrationDir) {
        this.orchestrationDir = orchestrationDir;
    }

    public boolean isImportNodes() {
        return importNodes;
    }

    public void setImportNodes(boolean importNodes) {
        this.importNodes = importNodes;
    }

    public Nodes getNodes() {
        return nodes;
    }

    public void setNodes(Nodes nodes) {
        this.nodes = nodes;
    }

    public NetworkDelays getNetworkDelays() {
        return networkDelays;
    }

    public void setNetworkDelays(NetworkDelays networkDelays) {
        this.networkDelays = networkDelays;
    }

    public Scaler getScaler() {
        return scaler;
    }

    public void setScaler(Scaler scaler) {
        this.scaler = scaler;
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public InitialSchedulingOrder getInitialSchedulingOrder() {
        return initialSchedulingOrder;
    }

    public void setInitialSchedulingOrder(InitialSchedulingOrder initialSchedulingOrder) {
        this.initialSchedulingOrder = initialSchedulingOrder;
    }

    public List<CustomNodes> getCustomNodes() {
        return customNodes;
    }

    public void setCustomNodes(List<CustomNodes> customNodes) {
        this.customNodes = customNodes;
    }

    public List<SchedulerPrio> getSchedulerPrio() {
        return schedulerPrio;
    }

    public void setSchedulerPrio(List<SchedulerPrio> schedulerPrio) {
        this.schedulerPrio = schedulerPrio;
    }

    public List<StartUpTimeContainer> getStartUpTimeContainer() {
        return startUpTimeContainer;
    }

    public void setStartUpTimeContainer(List<StartUpTimeContainer> startUpTimeContainer) {
        this.startUpTimeContainer = startUpTimeContainer;
    }

    public int getScalingInterval() {
        return scalingInterval;
    }

    public void setScalingInterval(int scalingInterval) {
        this.scalingInterval = scalingInterval;
    }

    public int getHealthCheckDelay() {
        return healthCheckDelay;
    }

    public void setHealthCheckDelay(int healthCheckDelay) {
        this.healthCheckDelay = healthCheckDelay;
    }

    public boolean isUseClusterAutoscaler() {
        return useClusterAutoscaler;
    }

    public void setUseClusterAutoscaler(boolean useClusterAutoscaler) {
        this.useClusterAutoscaler = useClusterAutoscaler;
    }

    public static class InitialSchedulingOrder {
        private boolean enabled;
        private List<String> order;

        public boolean isEnabled() {
            return enabled;
        }

        public List<String> getOrder() {
            return order;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setOrder(List<String> order) {
            this.order = order;
        }
    }

    public static class NetworkDelays {
        boolean enabled;
        Map<String, Map<String, NetworkInfo>> delayMap;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Map<String, Map<String, NetworkInfo>> getDelayMap() {
            return delayMap;
        }

        public void setDelayMap(Map<String, Map<String, NetworkInfo>> delayMap) {
            this.delayMap = delayMap;
        }

        public static class NetworkInfo {
            double mean;
            double std;

            public double getMean() {
                return mean;
            }

            public double getStd() {
                return std;
            }

            public void setMean(double mean) {
                this.mean = mean;
            }

            public void setStd(double std) {
                this.std = std;
            }
        }
    }

    public static class CustomNodes {
        String name;
        int cpu;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCpu() {
            return cpu;
        }

        public void setCpu(int cpu) {
            this.cpu = cpu;
        }
    }

    public static class SchedulerPrio {
        String name;
        int prio;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPrio() {
            return prio;
        }

        public void setPrio(int prio) {
            this.prio = prio;
        }
    }

    public static class StartUpTimeContainer {
        String name;
        int time;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }

    public static class Nodes {
        int amount;
        int cpu;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getCpu() {
            return cpu;
        }

        public void setCpu(int CPU) {
            this.cpu = CPU;
        }
    }

    public static class Scaler {

        boolean importScaler;
        List<ScalerSpec> scalerList;

        public Scaler() {
            importScaler = false;
            scalerList = new ArrayList<>();
        }

        public boolean isImportScaler() {
            return importScaler;
        }

        public List<ScalerSpec> getScalerList() {
            return scalerList;
        }

        public void setImportScaler(boolean importScaler) {
            this.importScaler = importScaler;
        }

        public void setScalerList(List<ScalerSpec> scalerList) {
            this.scalerList = scalerList;
        }
    }

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

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getScalerType() {
            return scalerType;
        }

        public void setScalerType(String scalerType) {
            this.scalerType = scalerType;
        }

        public double getTargetUtilization() {
            return targetUtilization;
        }

        public void setTargetUtilization(double targetUtilization) {
            this.targetUtilization = targetUtilization;
        }

        public int getMinReplicas() {
            return minReplicas;
        }

        public void setMinReplicas(int minReplicas) {
            this.minReplicas = minReplicas;
        }

        public int getMaxReplicas() {
            return maxReplicas;
        }

        public void setMaxReplicas(int maxReplicas) {
            this.maxReplicas = maxReplicas;
        }

        public double getLowerBound() {
            return lowerBound;
        }

        public void setLowerBound(double lowerBound) {
            this.lowerBound = lowerBound;
        }

        public double getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(double upperBound) {
            this.upperBound = upperBound;
        }

        public int getHoldTime() {
            return holdTime;
        }

        public void setHoldTime(int holdTime) {
            this.holdTime = holdTime;
        }

        public int getIncrement() {
            return increment;
        }

        public void setIncrement(int increment) {
            this.increment = increment;
        }

        public int getDecrement() {
            return decrement;
        }

        public void setDecrement(int decrement) {
            this.decrement = decrement;
        }
    }

}
