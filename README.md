## WIP Note: This ReadMe is still WIP.

# misim-orchestration - A MiSim plugin for simulating container orchestration

## Build from source

TODO
Clone via git and run
`mvn -B package --file pom.xml "-DskipTests=true" "-Dmaven.javadoc.skip=true" "-Dcheckstyle.skipExec=true"`. You should
see a `misim.jar` file in the resulting `target/` directory.

### Execution

TODO
Simply run `java -jar misim.jar [arguments]`.

### Parameters

| Argument | Short | Required | Description | Example |
|----------|------|----------|-------------|---------|
|   --arch_model       |   -a   |     true     |    provides path to the architecture file         |     ./Examples/example_architecture_scaling.json    |

## <a name="Execution"></a>Execution

The simulation works only when the relative path `./Report` exists in execution directory. With the following file
structure ...

```
project/
|--- Examples/
    |--- architecture_model.json
    |--- experiment_model.json
    |--- ...
|--- Report/
    |--- css/
    |--- js/
    |--- ...
|--- MiSim.jar
|--- ...
```

... use the following command to run a simulation:

`java -jar MiSim.jar -a ./Examples/architecture_model.json -e ./Examples/experiment_model.json -p`

## <a name="orchestration"></a>Orchestration
The orchestration plugin enables MiSim to support container orchestration tasks. It is enabled by...


###Architecture File
The architecture file is still necessary to run MiSim. 
It did not change but supports new values for the field 
- ___loadbalancer_strategy___:  [___leastUtil_orchestration___, ___random_orchestration___]


```json
{
  "microservices": [
    {
      "name": "users",
      "instances": 3,
      "loadbalancer_strategy": "leastUtil_orchestration",
      "patterns": [],
      "capacity": 5,
      "operations": [...]
    }
}
```

###<a name="experiment_orchestration"></a>Experiment File
The experiment file is extended. It needs the following fields for enabling orchestration mode
- ___orchestrate___: true/false - 
- ___orchestration_dir___: <<folder_path>> - Insert here the relative folder path, where your config and yaml files are located. 
The folder needs to bear the subdirectories, called ___environment___ (config.yaml inside), and ___k8_files___ (k8 yaml files inside). 

Additionally, the user can configure **Chaos Monkey for pods** similar to the usual Chaos Monkeys.
Instead of giving a ___microservice___ the user needs to insert a ___deployment___ by providing the corresponding name.
```json
{
  "simulation_meta_data": {
    "experiment_name": "ABCDE Experiment",
    "model_name": "architecture_model",
    "orchestrate": true,
    "orchestration_dir": "orchestration",
    "duration": 60,
    "report": "",
    "datapoints": 20,
    "seed": 979
  },
  "request_generators": [
    {
      "type": "interval",
      "config": {
        "microservice": "frontend",
        "operation": "createBook",
        "interval": 5
      }
    },
    {
      "type": "interval",
      "config": {
        "microservice": "books",
        "operation": "books.GET",
        "interval": 10
      }
    }
  ],

  "chaos_monkeys": [
    {
      "type": "monkey",
      "config":     {
        "microservice": "books",
        "instances": 1,
        "time": 5
      }
    },
    {
      "type": "monkey",
      "config":     {
        "microservice": "books",
        "instances": 1,
        "time": 20
      }
    }
  ],

  "chaos_monkeys_pods": [
    {
      "type": "monkey_pods",
      "config":     {
        "deployment": "frontend-deployment",
        "instances": 1,
        "time": 30
      }
    }
  ]
}
```

###The orchestration files
The orchestration files need to be placed in the orchestration folder that was explicitly specified in the 
experiment file (see [Experiment File](#experiment_orchestration)).

The project structure looks like this:

```
project/
|--- orchestration/
    |--- environment/
        |--- config.yaml
    |--- k8_files
        |--- books-deployment.yaml
        |--- hpa-books.yaml
|--- Examples/
    |--- architecture_model.json
    |--- experiment_model.json
    |--- ...
|--- Report/
    |--- css/
    |--- js/
    |--- ...
|--- MiSim.jar
|--- ...
```

#### config.yaml
A file called **config.yaml** needs to be provided inside the folder ___environment___:

```yaml
nodes:
  amount: 7
  cpu: 1500
customNodes:
  - name: frontend
    cpu: 1000
  - name: backend
    cpu: 1000
scaler:
  holdTimeUpScaler: 0
  holdTimeDownScaler: 300
loadBalancer: leastUtil_orchestration
scheduler: kube
scalingInterval: 15
healthCheckDelay: 0
schedulerPrio:
  - name: kube
    prio: 1
  - name: firstFit
    prio: 2
startUpTimeContainer:
  - name: frontend
    time: 0
  - name: backend
    time: 0
```

The config.yaml contains information necessary for the orchestration process:

- ___nodes___: Info about homogenous nodes 
  - ___amount___: Number of nodes in the cluster
  - ___cpu___: CPU capacity of each node in Mhz
- ___customNodes___: Possibility to add nodes with specified attributes
    - ___name___: The name of the node
    - ___cpu___: CPU capacity of this node in Mhz
- ___scaler___: Holds information about scaling restrictions
    - ___holdTimeUpScaler___: Time when upscaling is allowed in seconds after the last upscaling event
    - ___holdTimeDownScaler___: Time when downscaling is allowed in seconds after the last downscaling event
- ___loadBalancer___: Default loadbalancer that is used when not given in architecture file
  (possible values: ___leastUtil_orchestration___, ___random_orchestration___)
- ___scheduler___: Default scheduler that is used when not given in deployment file
  (possible values: ___firstFit___, ___roundRobin___, ___random___, ___kube___)
- ___scalingInterval___: Period for scaling frequency
- ___healthCheckDelay___: Customizable value which delays the scheduling of the healthCheck Event
- ___schedulerPrio___: Defines the order of scheduling during the simulation
  - ___name___: Name of the scheduler
  - ___prio___: Prio of the scheduler (lower numbers before higher ones)
- ___startUpTimeContainer___: Defines startup times for containers
    - ___name___: Name of the mircoservice
    - ___prio___: time needed for starting up [s]


#### k8 files
In this directory, deployment.yaml and corresponding Horizontal Pod Autoscalers (HPA) can be defined. However, 
none of these files are necessary to run the simulation. The simulation can also run with only the architecture file
given. The deployment logic looks like this:

 - if service is specified in k8s deployments and in architecture model -> one deployment created
 - if service is specified in k8s deployments but not in architecture model -> should result in a warning will
 not be created and not simulated
 - if service is not specified in k8s deployments but in the architecture model -> automatically create deployment,
 autoscaler, load balancer, scheduler etc. from default values or entry from architecture file

A deployment (for the microservices ___users___) looks like this:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-deployment
  labels:
    app: users
spec:
  replicas: 3
  selector:
    matchLabels:
      app: users
  template:
    metadata:
      labels:
        app: users
    spec:
      schedulerName: kube
      containers:
        - name: users
          image: nginx:1.14.2
```
Important values are:
- ___kind___: Defines the kind of the k8 object (here deployment)
- ___metadata/name___: Name of the deployment
- ___replicas___: Amount of pods that should be available
- ___schedulerName___: (**Optional**) Name of the scheduler
- ___containers/name___: Name of the microservice given in the architecture file. The names must be equal to 
match the microservice to the deployment's container

Furthermore, it is possible to specify a HPA for any deployment:

```yaml
apiVersion: autoscaling/v2beta2
kind: HorizontalPodAutoscaler
metadata:
  name: users-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: users-deployment
  minReplicas: 1
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 50
```
Important values are:
- ___kind___: Defines the kind of the k8 object (here HorizontalPodAutoscaler)
- ___metadata/name___: Name of the HPA
- ___scaleTargetRef/kind___: Kind of the object that should be managed by this HPA (always Deployment)
- ___scaleTargetRef/name___: Name of the deployment that should be managed by this HPA
- ___minReplicas___: Minimum amount of pods that should be available
- ___maxReplicas___: Maximum amount of pods that should be available
- ___metrics___: This whole block needs to be added like this. Until now only cpu utilization is supported
- ___metrics/averageUtilization___: Amount of average utilization that should be valid for all pods of this deployment 
in percent


### Kube-Scheduler
It is possible to use the real kube-scheduler as scheduler for the simulation.

Download scheduler here: https://www.downloadkubernetes.com/

Make the file executable 

Simulation with the kube-scheduler... 
- start the api with the command ```uvicorn main:app```
- start the scheduler with the command ```./kube-scheduler --master 127.0.0.1:8000```
- Run MiSim (with at least one deployment with the scheduler ___kube___)



#### Node Affinity
It is possible to define node affinities (see https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/)
Currently 
- exactly **one** requiredDuringSchedulingIgnoredDuringExecution, 
- **one** key ("personalized/name") and 
- **multiple** values (any String) are supported


```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: books-deployment
  labels:
    app: books
spec:
  replicas: 1
  selector:
    matchLabels:
      app: books
  template:
    metadata:
      labels:
        app: books
    spec:
      containers:
        - name: books
          image: booksApp:1.14.2
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
              - matchExpressions:
                  - key: personalized/name
                    operator: In
                    values:
                      - Large
```
