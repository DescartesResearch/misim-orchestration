# Pod Failure Example with Kube-Scheduler and Cluster-Autoscaler

In this example, we will use the kube-scheduler and cluster-autoscaler in an experiment with a simulated application
consisting of 8
microservices. The experiment will simulate a pod failure of one `usersService`-pod at simulation time 30, one `movieService`-pod at simulation time 60 and a
failure of one
`authService`-pod at simulation time 120. Note that the cluster-autoscaler will not perform scaling in this scenario
and is mainly
included for demonstration purposes. For a cluster scaling scenario, see
the [ClusterScalingExample](ClusterScalingExample.md).

## Prepare the adapter

Prerequisites: `go` (see https://go.dev/doc/install)

1. Download the adapter code from the [repository](https://github.com/DescartesResearch/misim-k8s-adapter)

   ```
   git clone https://github.com/DescartesResearch/misim-k8s-adapter.git
   ```

2. Build and run adapter code

   ```
   bash ./misim-k8s-adapter/run.sh
   ```

## Kube-Scheduler

1. Download the kube-scheduler artifact:

   ```
   wget -O kube-scheduler https://dl.k8s.io/v1.27.2/bin/linux/amd64/kube-scheduler
   ```

2. Add executable rights

   ```
   chmod +x kube-scheduler
   ```

3. Run kube-scheduler

   ```
   ./kube-scheduler --master localhost:8000 --config ./examples/ClusterSchedulerPodFailure/scheduler-config.yaml --leader-elect=false
   ```

## Cluster-Autoscaler

1. Unfortunately, cluster-autoscaler binaries are not officially provided. Therefore, either the cluster-autoscaler
   version `1.27.2` has to be built from source, or the cluster-autoscaler binary (x86-64) can be downloaded from the
   code ocean capsule of the Kubernetes-in-the-loop
   publication: [cluster-autoscaler](https://codeocean.com/capsule/5692428/tree/v1/data/cluster-autoscaler/cluster-autoscaler-amd64) (
   no direct wget command possible)

2. Add executable rights

   ```
   chmod +x cluster-autoscaler-amd64
   ```

3. Run cluster-autoscaler

- Either run the cluster-autoscaler with a random expansion strategy (default):

   ```
   ./cluster-autoscaler-amd64 --kubeconfig ./examples/ClusterSchedulerPodFailure/cluster_autoscaler-config.yaml --leader-elect=false --cloud-provider clusterapi --scan-interval=5s
   ```

- ...or with another strategy like `least-waste`:
   ```
   ./cluster-autoscaler-amd64 --kubeconfig ./examples/ClusterSchedulerPodFailure/cluster_autoscaler-config.yaml --leader-elect=false --cloud-provider clusterapi --scan-interval=5s --expander=least-waste
   ```
For more information about expansion strategies, see the [here](https://github.com/kubernetes/autoscaler/blob/master/cluster-autoscaler/FAQ.md#what-are-expanders)
## Build and run the simulation

1. Install MiSim dependencies (if the steps in the main [readme](../README.md) have not been executed yet)
    - Install the provided MiSim core dependency:
       ```
       mvn install:install-file \
         -Dfile=./libraries/misim-3.3.1.jar \
         -DgroupId=cambio.simulator \
         -DartifactId=misim \
         -Dversion=3.3.1 \
         -Dpackaging=jar \
         -DgeneratePom=true
       ```
    - Install the provided MiSim tests dependency:
       ```
       mvn install:install-file \
       -Dfile=./libraries/misim-tests-3.3.1.jar \
       -DgroupId=cambio.simulator \
       -DartifactId=misim \
       -Dversion=3.3.1 \
       -Dpackaging=jar \
       -Dclassifier=tests \
       -DgeneratePom=true
       ```

2. Build this repository
   ```
   mvn clean package
   ```
3. Run the simulation (also provided as an IntelliJ run configuration)

   ```
   java -jar ./target/misim-orchestration.jar \
     -a ./examples/ClusterSchedulerPodFailure/cspf_architecture_model.json \
     -e ./examples/ClusterSchedulerPodFailure/cspf_example_model.json \
     --orchestration ./examples/ClusterSchedulerPodFailure/cspf_orchestration_config.yaml \
     -d
   ```