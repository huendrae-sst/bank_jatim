package com.bankjatim.jims.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    @Test
    void usesJackson3MessageConverter() {
        assertThat(new RabbitMQConfig().jsonMessageConverter())
                .isInstanceOf(JacksonJsonMessageConverter.class);
    }
}
