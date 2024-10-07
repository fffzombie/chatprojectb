package com.zombie.chatglm.data.domain.openai.service.channel.impl;

import com.alibaba.fastjson.JSON;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.service.channel.OpenAiGroupService;
import com.zombie.chatglm.data.types.exception.ChatGPTException;
import com.zombie.chatgpt.common.Constants;
import com.zombie.chatgpt.domain.chat.ChatChoice;
import com.zombie.chatgpt.domain.chat.ChatCompletionRequest;
import com.zombie.chatgpt.domain.chat.ChatCompletionResponse;
import com.zombie.chatgpt.domain.chat.Message;
import com.zombie.chatgpt.session.OpenAiSession;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: ChatGPTService
 * Package: com.zombie.chatglm.data.domain.openai.service.channel.impl
 * Description:ChatGPT服务
 *
 * @Author ME
 * @Create 2024/10/5 16:42
 * @Version 1.0
 */
@Slf4j
@Service
public class ChatGPTService implements OpenAiGroupService {
    @Resource
    protected OpenAiSession chatGPTOpenAiSession;
    @Timed(value = "chatgptmodel_domessage_response",description = "chatgpt模型对话次数")
    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcessAggregate, ResponseBodyEmitter emitter) throws Exception {
        //1.请求消息
        List<Message> messages = chatProcessAggregate.getMessages().stream()
                .map(entity -> Message.builder()
                        .role(Constants.Role.valueOf(entity.getRole().toUpperCase()))
                        .content(entity.getContent())
                        .build())
                .collect(Collectors.toList());


        //2.封装参数
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(messages)
                .model(chatProcessAggregate.getModel())
                .build();

        //3.请求应答
        chatGPTOpenAiSession.chatCompletions(chatCompletionRequest, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                List<ChatChoice> choices = chatCompletionResponse.getChoices();
                for (ChatChoice choice : choices) {
                    Message delta = choice.getDelta();
                    if(Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;

                    //应答完成
                    String finishReason = choice.getFinishReason();
                    if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                        emitter.complete();
                        break;
                    }

                    //返回消息
                    try {
                        //TODO 加入睡眠时间，否则会无法实现流式传输的视觉效果，待优化
                        Thread.sleep(10);
                        emitter.send(delta.getContent());
                    } catch (Exception e) {
                        throw new ChatGPTException(e.getMessage());
                    }
                }
            }
        });

    }
}
