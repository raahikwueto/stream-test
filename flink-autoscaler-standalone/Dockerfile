FROM alpine:latest

RUN apk add openjdk11 --no-cache bash libc6-compat

WORKDIR /
COPY wait.sh wait.sh
COPY target/flink-autoscaler-standalone-1.9-SNAPSHOT.jar .

CMD java -cp flink-autoscaler-standalone-1.9-SNAPSHOT.jar org.apache.flink.autoscaler.standalone.StandaloneAutoscalerEntrypoint --flinkClusterHost=localhost, --flinkClusterPort=8082