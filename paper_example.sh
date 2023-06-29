#!/usr/bin/env bash

mvn install:install-file -Dfile=./libraries/misim-3.2.1.jar -DgroupId="cambio.simulator" -DartifactId=misim -Dversion="3.2.1" -Dpackaging=jar -DgeneratePom=true

mvn clean package -B --file pom.xml "-Dmaven.javadoc.skip=true"

java -jar ./target/misim-orchestration.jar -a ./BasicExample/paper_architecture_model.json -e ./BasicExample/paper_example_model.json -d --orchestration ./BasicExample/paper_orchestration_config.yaml

