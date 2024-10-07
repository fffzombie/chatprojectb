package com.zombie.chatglm.data.domain.openai.service.channel.impl;

import com.alibaba.fastjson.JSON;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.service.channel.OpenAiGroupService;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import com.zombie.chatglm.model.ChatCompletionRequest;
import com.zombie.chatglm.model.ChatCompletionResponse;
import com.zombie.chatglm.model.Model;
import com.zombie.chatglm.session.OpenAiSession;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: ChatGLMService
 * Package: com.zombie.chatglm.data.domain.openai.service.channel.impl
 * Description:ChatGLM服务
 *
 * @Author ME
 * @Create 2024/10/5 16:42
 * @Version 1.0
 */
@Slf4j
@Service
public class ChatGLMService implements OpenAiGroupService {
    @Resource
    protected OpenAiSession chatGlMOpenAiSession;
    @Timed(value = "chatglmmodel_domessage_response",description = "chatglm模型对话次数")
    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcessAggregate, ResponseBodyEmitter emitter) throws Exception {
        //1.请求消息
        List<ChatCompletionRequest.Prompt> messages = chatProcessAggregate.getMessages().stream()
                .map(entity -> ChatCompletionRequest.Prompt.builder()
                        .role(entity.getRole())
                        .content(entity.getContent())
                        .build())
                .collect(Collectors.toList());


        //2.封装参数
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Model.getModelByCode(chatProcessAggregate.getModel()));
        request.setPrompt(messages);

        //3.请求应答
        chatGlMOpenAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                ChatCompletionResponse response = JSON.parseObject(data,ChatCompletionResponse.class);

                //应答完成
                if("[DONE]".equals(data)){
                    log.info("[输出结束] Tokens {}", JSON.toJSONString(data));
                    emitter.complete();
                }

                //发送信息
                try {
                    emitter.send(response.getData());
                } catch (IOException e) {
                    throw new ChatGLMException(e.getMessage());
                }


            }

        });


    }
}
