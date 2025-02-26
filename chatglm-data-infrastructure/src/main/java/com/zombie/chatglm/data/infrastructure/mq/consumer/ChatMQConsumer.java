package com.zombie.chatglm.data.infrastructure.mq.consumer;

import com.zombie.chatglm.data.domain.openai.model.event.ChatMessageEvent;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.infrastructure.repository.OpenAiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ChatMQConsumer {

    @Resource
    private OpenAiRepository openAiRepository;

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "chat.exchange"),
            value = @Queue(name = "chat.update.queue"),
            key = "chat.update.message"
    ))
    public void handleSendUpdateSessionMessage(ChatMessageEvent event) {
        openAiRepository.updateSessionMessage(SessionMessageVO.builder()
                        .sendTime(event.getSessionMessageVO().getSendTime())
                        .content(event.getSessionMessageVO().getContent())
                        .role(event.getSessionMessageVO().getRole())
                .build(),event.getChatProcess());
    }

}
