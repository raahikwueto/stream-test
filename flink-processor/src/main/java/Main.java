import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.common.TopicPartition;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.common.functions.MapFunction;


import java.util.Arrays;
import java.util.HashSet;


public class Main {
    static final String BROKERS  = "kafka:9092";

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        System.out.println("Environment created");


        KafkaSource<Health> source = KafkaSource.<Health>builder()
                .setBootstrapServers(BROKERS)
                .setProperty("partition.discovery.interval.ms", "1000")
                .setTopics("health")
                .setGroupId("groupId-919292")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new HealthDeserializationSchema())
                .build();

        DataStreamSource<Health> kafka = env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka");

        System.out.println("kafka created");

        DataStream<Tuple2<MyAverage, Double>> averageTemperatureStream = kafka.keyBy(myEvent -> myEvent.name)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
                .aggregate(new AverageAggregator());

        DataStream<Tuple2<String, Double>> nameAndValueStream = averageTemperatureStream
                .map(new MapFunction<Tuple2<MyAverage, Double>, Tuple2<String, Double>>() {
                    @Override
                    public Tuple2<String, Double> map(Tuple2<MyAverage, Double> input) throws Exception {
                    return new Tuple2<>(input.f0.name, input.f1);
            }
        });

        System.out.println("Aggregation created");
        nameAndValueStream.print();

//        nameAndValueStream.addSink(JdbcSink.sink("insert into person (name, temperature) values (?, ?)",
//                (statement, event) -> {
//                    statement.setString(1, event.f0);
//                    statement.setDouble(2, event.f1);
//                },
//                JdbcExecutionOptions.builder()
//                        .withBatchSize(1000)
//                        .withBatchIntervalMs(200)
//                        .withMaxRetries(5)
//                        .build(),
//                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
//                        .withUrl("jdbc:postgresql://localhost:5438/postgres")
//                        .withDriverName("org.postgresql.Driver")
//                        .withUsername("postgres")
//                        .withPassword("postgres")
//                        .build()
//        ));
        env.execute("Streaming-test-project");
    }

    /**
     * Aggregation function for average.
     */
    public static class AverageAggregator implements AggregateFunction<Health, MyAverage, Tuple2<MyAverage, Double>>{

        @Override
        public MyAverage createAccumulator(){
            return new MyAverage();
        }

        @Override
        public MyAverage add(Health health, MyAverage myAverage){
            myAverage.name = health.name;
            myAverage.count = myAverage.count + 1;
            myAverage.sum = myAverage.sum + health.temperature;
            return myAverage;
        }

        @Override
        public Tuple2<MyAverage, Double> getResult(MyAverage myAverage) {
            return new Tuple2<>(myAverage, myAverage.sum / myAverage.count);
        }

        @Override
        public MyAverage merge(MyAverage myAverage, MyAverage acc1) {
            myAverage.sum = myAverage.sum + acc1.sum;
            myAverage.count = myAverage.count + acc1.count;
            return myAverage;
        }
    }

    public static class MyAverage {

        public String name;
        public Integer count = 0;
        public Double sum = 0d;

        @Override
        public String toString() {
            return "MyAverage{" +
                    "name='" + name + '\'' +
                    ", count=" + count +
                    ", sum=" + sum +
                    '}';
        }
    }
}