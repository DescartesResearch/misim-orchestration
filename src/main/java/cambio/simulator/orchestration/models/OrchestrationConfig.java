package cambio.simulator.orchestration.models;

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
    private String healthCheckDelay;
    private List<CustomNodes> customNodes;
    private List<SchedulerPrio> schedulerPrio;
    private List<StartUpTimeContainer> startUpTimeContainer;

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

    public String getHealthCheckDelay() {
        return healthCheckDelay;
    }

    public void setHealthCheckDelay(String healthCheckDelay) {
        this.healthCheckDelay = healthCheckDelay;
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
        int holdTimeUpScaler;
        int holdTimeDownScaler;

        public int getHoldTimeUpScaler() {
            return holdTimeUpScaler;
        }

        public void setHoldTimeUpScaler(int holdTimeUpScaler) {
            this.holdTimeUpScaler = holdTimeUpScaler;
        }

        public int getHoldTimeDownScaler() {
            return holdTimeDownScaler;
        }

        public void setHoldTimeDownScaler(int holdTimeDownScaler) {
            this.holdTimeDownScaler = holdTimeDownScaler;
        }
    }

}
