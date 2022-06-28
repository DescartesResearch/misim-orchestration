package cambio.simulator.orchestration.models;

import java.util.List;

public class OrchestrationConfig {

    private boolean orchestrate;
    private String orchestrationDir;
    private Nodes nodes;
    private Scaler scaler;
    private String scheduler;
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

    public Nodes getNodes() {
        return nodes;
    }

    public void setNodes(Nodes nodes) {
        this.nodes = nodes;
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
