#!/usr/bin/env bash

mvn install:install-file -Dfile=./libraries/misim-3.2.jar -DgroupId="cambio.simulator" -DartifactId=misim -Dversion="3.2" -Dpackaging=jar -DgeneratePom=true

mvn clean package -B --file pom.xml "-Dmaven.javadoc.skip=true"

java -jar ./target/misim-orchestration.jar -a ./BasicExample/architecture_model.json -e ./BasicExample/example_model.json --orchestration ./BasicExample/orchestration_config.yaml -d

