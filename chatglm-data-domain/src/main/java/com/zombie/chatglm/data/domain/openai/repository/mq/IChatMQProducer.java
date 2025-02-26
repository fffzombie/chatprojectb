package com.zombie.chatglm.data.domain.openai.repository.mq;

import com.zombie.chatglm.data.domain.openai.model.event.ChatMessageEvent;

public interface IChatMQProducer {
    void sendUpdateSessionMessage(ChatMessageEvent event);
}
