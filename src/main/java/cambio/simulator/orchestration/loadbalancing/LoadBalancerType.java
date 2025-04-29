package cambio.simulator.orchestration.loadbalancing;

public enum LoadBalancerType {

    RANDOM("RandomLoadBalancer"),
    LEAST_UTIL("LeastUtilBalancer"),
    EVEN("EvenLoadBalancer"),
    QUICK_ROUND_ROBIN("QuickRoundRobinLoadBalancer"),
    ROUND_ROBIN("RoundRobinLoadBalancer"),
    TOPOLOGY_AWARE("TopologyAwareLoadBalancer");

    LoadBalancerType(String displayName) {
        this.displayName = displayName;
    }

    final String displayName;

    public String getDisplayName() {
        return displayName;
    }
}
