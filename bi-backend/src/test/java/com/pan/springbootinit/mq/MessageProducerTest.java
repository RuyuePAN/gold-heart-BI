package com.pan.springbootinit.mq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageProducerTest {
    @Resource
    private MessageProducer messageProducer;

    @Test
    void sendMessage() {
        messageProducer.sendMessage("code_exchange", "my_routingKey", "这是一条发送的消息");
    }

}