# misim-orchestration - A MiSim plugin for simulating container orchestration

## Prerequisites

- Apache Maven (`mvn`)
- Java 8+

## Build

1. Install MiSim dependency
    - Core dependency from the [source repo](https://github.com/Cambio-Project/MiSim) or using the provided library:

       ```
       mvn install:install-file \
         -Dfile=./libraries/misim-3.3.1.jar \
         -DgroupId=cambio.simulator \
         -DartifactId=misim \
         -Dversion=3.3.1 \
         -Dpackaging=jar \
         -DgeneratePom=true
       ```
    - (Optional) For running tests, install MiSim tests dependency:

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

## Examples

Read
our [hello-world example](https://github.com/DescartesResearch/misim-orchestration/blob/main/docs/HelloWorldExample.md)
to learn about how to run simulations.

## Configuration options

For configuration options of the MiSim core, architecture and experiment model syntax, we refer to
the [MiSim repository](https://github.com/Cambio-Project/MiSim).
The configuration options of this orchestration plugin are
described [here](https://github.com/DescartesResearch/misim-orchestration/blob/main/docs/ConfigurationOptions.md).

## Cite us

```
@inproceedings{straesser2023kubernetesintheloop,
  abstract = {Microservices deployed and managed by container orchestration frameworks like Kubernetes are the bases of modern cloud applications. In microservice performance modeling and prediction, simulations provide a lightweight alternative to experimental analysis, which requires dedicated infrastructure and a laborious setup. However, existing simulators cannot run realistic scenarios, as performance-critical orchestration mechanisms (like scheduling or autoscaling) are manually modeled and can consequently not be represented in their full complexity and configuration space. This work combines a state-of-the-art simulation for microservice performance with Kubernetes container orchestration. Hereby, we include the original implementation of Kubernetes artifacts enabling realistic scenarios and testing of orchestration policies with low overhead. In two experiments with Kubernetes' kube-scheduler and cluster-autoscaler, we demonstrate that our framework can correctly handle different configurations of these orchestration mechanisms boosting both the simulation's use cases and authenticity.},
  added-at = {2023-08-17T01:05:43.000+0200},
  author = {Straesser, Martin and Haas, Patrick and Frank, Sebastian and Hakamian, Alireza and Van Hoorn, André and Kounev, Samuel},
  biburl = {https://www.bibsonomy.org/bibtex/23ea9a74ebfc49b6a1a29bce1d6083855/samuel.kounev},
  booktitle = {Performance Evaluation Methodologies and Tools},
  interhash = {373d040402db63c40b7b0b707adf66ad},
  intrahash = {3ea9a74ebfc49b6a1a29bce1d6083855},
  keywords = {cloud_computing container_orchestration descartes discrete_event_simulation kubernetes microservices software_performance t_full myown},
  note = {In print.},
  timestamp = {2023-08-17T01:05:43.000+0200},
  title = {Kubernetes-in-the-Loop: Enriching Microservice Simulation Through Authentic Container Orchestration},
  year = 2023
}
```

## Any questions?

For questions
contact [Martin Straesser](https://se.informatik.uni-wuerzburg.de/software-engineering-group/staff/martin-straesser/).