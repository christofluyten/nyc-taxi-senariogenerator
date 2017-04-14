#!/usr/bin/env bash
export MAVEN_OPTS=-server
nohup mvn clean compile -U exec:java -Dexec.mainClass="com.github.rinde.rinsim.geom.RoutingTableHandler" &
