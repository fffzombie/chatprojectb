package com.zombie.chatglm.data.infrastructure.mq;

import com.zombie.chatglm.data.domain.openai.model.event.ChatMessageEvent;
import com.zombie.chatglm.data.domain.openai.repository.mq.IChatMQProducer;
import com.zombie.chatglm.data.types.enums.MQExchanges;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ChatMQProducer implements IChatMQProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendUpdateSessionMessage(ChatMessageEvent event) {
        rabbitTemplate.convertAndSend(MQExchanges.CHAT_ROUTER.getRouterName(), "chat.update.message", event);
    }
}
