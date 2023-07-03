# Hello World Example

In this example, we will use the kube-scheduler in an experiment with a simulated application consisting of 8 
microservices.

## Prepare the adapter

Prerequisites: `go`

1. Download the adapter code from the [repository](https://github.com/DescartesResearch/misim-k8s-adapter)

```
git clone https://github.com/DescartesResearch/misim-k8s-adapter.git
```

2. Build and run adapter code

```
bash ./misim-k8s-adapter/run.sh
```

## Prepare the kube-scheduler

1. Download the scheduler artifact of build from [source](https://github.com/kubernetes/kubernetes)

```
wget -O kube-scheduler https://dl.k8s.io/v1.27.2/bin/linux/amd64/kube-scheduler
```

2. Add executable rights

```
chmod +x kube-scheduler
```

3. Run kube-scheduler

```
./kube-scheduler \
    --master localhost:8000 \
    --config ./examples/HelloWorldExample/scheduler-config.yaml \
    --leader-elect=false
```

## Run the simulation

1. Build the simulation

```
mvn install:install-file \
    -Dfile=./libraries/misim-3.2.5.jar \
    -DgroupId="cambio.simulator" \
    -DartifactId=misim \
    -Dversion="3.2.5" \
    -Dpackaging=jar \
    -DgeneratePom=true
mvn clean package
```

2. Run the simulation

```
java -jar ./target/misim-orchestration.jar \
  -a ./examples/HelloWorldExample/architecture_model.json \
  -e ./examples/HelloWorldExample/example_model.json \
  --orchestration ./examples/HelloWorldExample/orchestration_config.yaml \
  -d
```