# misim-orchestration - A MiSim plugin for simulating container orchestration

## Prerequisites

- Apache Maven (`mvn`)
- Java 8+

## Build

1. Install MiSim core dependency from [source repo](https://github.com/Cambio-Project/MiSim) or using the provided library: 

```
mvn install:install-file \
  -Dfile=./libraries/misim-3.2.5-SNAPSHOT.jar \
  -DgroupId=cambio.simulator \
  -DartifactId=misim \
  -Dversion=3.2.5-SNAPSHOT \
  -Dpackaging=jar \
  -DgeneratePom=true
```

2. Build this repository

```
mvn clean package
```

## Examples

Read our [hello-world example](https://github.com/DescartesResearch/misim-orchestration/blob/main/docs/HelloWorldExample.md) to learn about how to run simulations.

## Configuration options

For configuration options of the MiSim core, architecture and experiment model syntax, we refer to the [MiSim repository](https://github.com/Cambio-Project/MiSim).
The configuration options of this orchestration plugin are described [here](https://github.com/DescartesResearch/misim-orchestration/blob/main/docs/ConfigurationOptions.md).

## Cite us

The paper related to this repository is currently under review. We will add citation info as soon as available.

## Any questions?

For questions contact [Martin Straesser](https://se.informatik.uni-wuerzburg.de/software-engineering-group/staff/martin-straesser/).