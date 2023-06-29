package cambio.simulator.orchestration.util;

import cambio.simulator.entities.patterns.*;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.loadbalancing.IOrchestrationLoadBalancingStrategy;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.loadbalancing.*;
import cambio.simulator.orchestration.management.DefaultValues;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.kubernetes.KubeScheduler;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.orchestration.scheduling.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    private static final Util instance = new Util();

    //private constructor to avoid client applications to use constructor
    private Util() {}

    public static Util getInstance() {
        return instance;
    }

    public Set<String> listFilesUsingJavaIO(String dir) throws ParsingException {
        final File directory = new File(dir);
        if(directory.exists()){
            return Stream.of(directory.listFiles())
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toSet());
        }
        throw new ParsingException("Could not find the directory: " + dir);
    }

    public Deployment findDeploymentByName(String name){
        final Optional<Deployment> first =
                ManagementPlane.getInstance().getDeployments().stream().filter(deployment -> deployment.getPlainName().equals(name)).findFirst();
        return first.orElse(null);
    }

    public static <T extends Enum<?>> T searchEnum(Class<T> enumeration,
                                                   String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    public SchedulerType getSchedulerTypeByNameOrStandard(String schedulerName, String deploymentName) {
        if (schedulerName != null) {
            final SchedulerType schedulerType = SchedulerType.fromString(schedulerName);
            if (schedulerType != null) {
                return schedulerType;
            } else {
                System.out.print("[WARNING]: Unknown scheduler name '" + schedulerName + "' for deployment " + deploymentName + ".");
            }
        } else {
            System.out.print("[INFO]: No scheduler was selected for deployment " + deploymentName + ".");
        }
        System.out.println(" Using default Scheduler '" + SchedulerType.fromString(DefaultValues.getInstance().getScheduler()).getDisplayName() + "'");
        return SchedulerType.fromString(DefaultValues.getInstance().getScheduler());
    }

    public Scheduler getSchedulerInstanceByType(SchedulerType schedulerType) {
        if(schedulerType.equals(SchedulerType.RANDOM)){
            return RandomScheduler.getInstance();
        } else if(schedulerType.equals(SchedulerType.FIRSTFIT)){
            return FirstFitScheduler.getInstance();
        } else if(schedulerType.equals(SchedulerType.KUBE)) {
            return KubeScheduler.getInstance();
        }else if(schedulerType.equals(SchedulerType.ROUNDROBIN)) {
            return RoundRobinScheduler.getInstance();
        }
        throw new IllegalStateException("This SchedulerType is not linked to a Schedulerinstance yet. Do it here!");
    }

    public void connectLoadBalancer(MicroserviceOrchestration microserviceOrchestration) {
        // If no load balancer specified we use the random load balancer (same as misim)
        ILoadBalancingStrategy loadBalancingStrategy = microserviceOrchestration.getLoadBalancer().getLoadBalancingStrategy();
        IOrchestrationLoadBalancingStrategy convertedStrategy;
        String name;
        if (loadBalancingStrategy instanceof RandomLoadBalanceStrategy) {
            convertedStrategy = new RandomLoadBalanceStrategyOrchestration();
            name = LoadBalancerType.RANDOM.getDisplayName();
        } else if (loadBalancingStrategy instanceof UtilizationBalanceStrategy) {
            convertedStrategy = new LeastUtilizationLoadBalanceStrategyOrchestration();
            name = LoadBalancerType.LEAST_UTIL.getDisplayName();
        } else if (loadBalancingStrategy instanceof EvenLoadBalanceStrategy) {
            convertedStrategy = new EvenLoadBalanceStrategyOrchestration();
            name = LoadBalancerType.EVEN.getDisplayName();
        } else if (loadBalancingStrategy instanceof RoundRobinLoadbalancer) {
            convertedStrategy = new RoundRobinLoadBalanceStrategyOrchestration();
            name = LoadBalancerType.ROUND_ROBIN.getDisplayName();
        } else if (loadBalancingStrategy instanceof QuickRoundRobinLoadbalancer) {
            convertedStrategy = new QuickRRLoadBalanceStrategyOrchestration();
            name = LoadBalancerType.QUICK_ROUND_ROBIN.getDisplayName();
        } else {
            System.out.println("[WARNING] Unknown Load Balancing Strategy: " + loadBalancingStrategy);
            System.out.println("Using default: random");
            convertedStrategy = new RandomLoadBalanceStrategyOrchestration();
            name = LoadBalancerType.RANDOM.getDisplayName();
        }
        microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(ManagementPlane.getInstance().getModel(), name, ManagementPlane.getInstance().getModel().traceIsOn(), convertedStrategy, microserviceOrchestration));
    }

    public static long nanoSecondsToMilliSeconds(long nanosecs) {
        return TimeUnit.MILLISECONDS.convert(nanosecs, TimeUnit.NANOSECONDS);
    }
}
