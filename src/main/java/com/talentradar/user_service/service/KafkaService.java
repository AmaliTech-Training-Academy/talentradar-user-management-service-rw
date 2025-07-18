package com.talentradar.user_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.talentradar.user_service.dto.UserCreatedEvent;

@Service
public class KafkaService {
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${topic.user-created}")
    private String userCreatedTopic;

    @Value("${topic.user-updated}")
    private String userUpdatedTopic;

    public void sendUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        kafkaTemplate.send(userCreatedTopic, userCreatedEvent);
    }

    public void sendUserUpdatedEvent(UserCreatedEvent userUpdatedEvent) {
        kafkaTemplate.send(userUpdatedTopic, userUpdatedEvent);
    }

}
