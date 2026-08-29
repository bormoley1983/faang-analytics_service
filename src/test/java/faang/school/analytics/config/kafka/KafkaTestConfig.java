package faang.school.analytics.config.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class KafkaTestConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${spring.kafka.topics.comment-topic.name}")
    private String commentTopicName;

    @Value("${spring.kafka.topics.user-profile-view-topic.name}")
    private String profileViewTopicName;

    @Value("${app.kafka.topics.analytics.like}")
    private String likeTopicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(adminProps);
    }

    @Bean
    public NewTopic analyticsCommentTopic() {
        return TopicBuilder.name(commentTopicName)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic analyticsProfileViewTopic() {
        return TopicBuilder.name(profileViewTopicName)
            .partitions(1)
            .replicas(1)
            .build();
    }

    
    @Bean
    public NewTopic analyticsLikeTopic() {
        return TopicBuilder.name(likeTopicName)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic analyticsCommentDeadLetterTopic() {
        return TopicBuilder.name(commentTopicName + ".DLT").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic analyticsProfileViewDeadLetterTopic() {
        return TopicBuilder.name(profileViewTopicName + ".DLT").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic analyticsLikeDeadLetterTopic() {
        return TopicBuilder.name(likeTopicName + ".DLT").partitions(1).replicas(1).build();
    }
}
