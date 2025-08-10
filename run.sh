#!/usr/bin/env bash

./gradlew clean bootJar -Pvaadin.productionMode
java -jar build/libs/spring-ai-example-*.jar