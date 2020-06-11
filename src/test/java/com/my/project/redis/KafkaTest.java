package com.my.project.redis;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KafkaTest {

    @ClassRule
    public static final SharedKafkaTestResource EMBEDDED_KAFKA = new SharedKafkaTestResource()
            .withBrokers(1)
            .withBrokerProperty("auto.create.topics.enable", "false");

    @Test
    public void testKafka() throws InterruptedException, ExecutionException {

        String topicName = "TEST";

        List<TopicPartition> partitions = createTopic(topicName);
        produce(topicName);
        consume(topicName, partitions);
    }

    private List<TopicPartition> createTopic(String topicName) throws InterruptedException, ExecutionException {
        Properties config = new Properties();
        config.put("bootstrap.servers", EMBEDDED_KAFKA.getKafkaConnectString());
        try(AdminClient admin = AdminClient.create(config)) {
            CreateTopicsResult result = admin.createTopics(
                    Arrays.asList(new NewTopic(topicName, 1, (short)1)));
            while (!result.all().isDone()) {
                TimeUnit.SECONDS.sleep(1);
            }
            DescribeTopicsResult descFuture = admin.describeTopics(Arrays.asList(topicName));
            TopicDescription desc = descFuture.values().get(topicName).get();
            return desc.partitions().stream().map(partitionInfo ->
                    new TopicPartition(topicName, partitionInfo.partition())).collect(Collectors.toList());
        }
    }

    private void produce(String topicName) throws InterruptedException, ExecutionException {
        Properties config = new Properties();
        config.put("bootstrap.servers", EMBEDDED_KAFKA.getKafkaConnectString());
        config.put("acks", "all");
        config.put("retries", 0);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        try(Producer<String, String> producer = new KafkaProducer<String, String>(config)) {
            producer.send(new ProducerRecord<>(topicName, "key1", "value1")).get();
            producer.send(new ProducerRecord<>(topicName, "key2", "value2")).get();
            producer.send(new ProducerRecord<>(topicName, "key3", "value3")).get();
            producer.flush();
        }
    }

    private void consume(String topicName, List<TopicPartition> partitions) {
        Properties config = new Properties();
        config.put("bootstrap.servers", EMBEDDED_KAFKA.getKafkaConnectString());
        config.put("group.id", "test");
        config.put("enable.auto.commit", "true");
        config.put("auto.commit.interval.ms", 1000);
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        try(Consumer<String, String> consumer = new KafkaConsumer<String, String>(config)) {
            //consumer.subscribe(Arrays.asList(topicName));
            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);
            int loop = 2;
            while(loop-- > 0) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, offset = %d, key = %s, value = %s%n", record.topic(), record.offset(), record.key(), record.value());
                }
            }
        }
    }

}
