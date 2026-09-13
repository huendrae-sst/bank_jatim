package com.bankjatim.jims.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_JIMS = "jims.direct.exchange";
    public static final String QUEUE_NOTIFICATIONS = "jims.notifications.queue";
    public static final String QUEUE_EMBOSS_PROCESSING = "jims.emboss.queue";
    public static final String ROUTING_KEY_NOTIFICATIONS = "jims.routing.notifications";
    public static final String ROUTING_KEY_EMBOSS = "jims.routing.emboss";

    @Bean
    public DirectExchange jimsExchange() {
        return new DirectExchange(EXCHANGE_JIMS);
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Queue embossProcessingQueue() {
        return QueueBuilder.durable(QUEUE_EMBOSS_PROCESSING).build();
    }

    @Bean
    public Binding bindingNotifications(Queue notificationsQueue, DirectExchange jimsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(jimsExchange).with(ROUTING_KEY_NOTIFICATIONS);
    }

    @Bean
    public Binding bindingEmboss(Queue embossProcessingQueue, DirectExchange jimsExchange) {
        return BindingBuilder.bind(embossProcessingQueue).to(jimsExchange).with(ROUTING_KEY_EMBOSS);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
