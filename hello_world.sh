#!/usr/bin/env bash

mvn install:install-file -Dfile=C:\Users\mas00fr\IdeaProjects\misim-orchestration-github\libraries\misim-3.1.jar -DgroupId="cambio.simulator" -DartifactId=misim -Dversion="3.1" -Dpackaging=jar -DgeneratePom=true

mvn clean package -B --file pom.xml "-Dmaven.javadoc.skip=true"

java -jar ./target/misim-orchestration.jar -a .\BasicExample/architecture_model.json -e .\BasicExample/example_model.json --orchestration .\BasicExample/orchestration_config.json -d

