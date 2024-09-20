package com.zombie.chatglm.data.domain.openai.service;
/*
*
* 业务具体实现
* */

import cn.bugstack.chatglm.model.*;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService extends AbstractChatService{

    @Override
    protected void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws Exception {

        //1.请求消息
        List<ChatCompletionRequest.Prompt> messages = chatProcess.getMessages().stream()
                .map(entity -> ChatCompletionRequest.Prompt.builder()
                        .role(entity.getRole())
                        .content(entity.getContent())
                        .build())
                .collect(Collectors.toList());


        //2.封装参数
        ChatCompletionRequest request = new ChatCompletionRequest();
        //TODO:暂时写死model的类型
        request.setModel(Model.GLM_3_TURBO);
        request.setPrompt(messages);

        //3.请求应答
        openAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                ChatCompletionResponse response = JSON.parseObject(data,ChatCompletionResponse.class);
                //发送信息
                try {
                    responseBodyEmitter.send(response.getData());
                } catch (IOException e) {
                    throw new ChatGLMException(e.getMessage());
                }

//                responseBodyEmitter.complete();
//                if(EventType.finish.getCode().equals(type)){
//                    log.info("对话完成");
//                    responseBodyEmitter.complete();
//                }
            }

            //应答完成
            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                log.info("对话完成");
                responseBodyEmitter.complete();
            }
        });


    }
}
