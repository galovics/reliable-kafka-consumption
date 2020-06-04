package com.arnoldgalovics.blog.normaltopicconsumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Slf4j
public class NormalTopicConsumer {
    public static final String RETRY_COUNT_HEADER_KEY = "retryCount";
    public static final String ORIGINAL_TOPIC_HEADER_KEY = "originalTopic";
    public static final String DLQ_TOPIC = "dlq-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(id = "normal-topic-consumer", groupId = "normal-topic-group", topics = "normal-topic")
    public void consume(ConsumerRecord<?, ?> consumerRecord, Acknowledgment ack) {
        String json = consumerRecord.value().toString();
        try {
            log.info("Consuming normal message {}", json);
            throw new RuntimeException();
        } catch (Exception e) {
            log.info("Message consumption failed for message {}", json);
            String originalTopic = consumerRecord.topic();
            ProducerRecord<String, String> record = new ProducerRecord<>(DLQ_TOPIC, json);
            record.headers().add(ORIGINAL_TOPIC_HEADER_KEY, originalTopic.getBytes(UTF_8));

            Header retryCount = consumerRecord.headers().lastHeader(RETRY_COUNT_HEADER_KEY);
            if (retryCount != null) {
                record.headers().add(retryCount);
            }
            kafkaTemplate.send(record);
        } finally {
            ack.acknowledge();
        }
    }
}
