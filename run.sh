#!/bin/bash
./mvnw clean test install
echo "build success, running the program..."

java -server -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:ParallelGCThreads=4 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log \
-jar ./target/translate-files.jar Set1.txt Set2.txt Set3.txt