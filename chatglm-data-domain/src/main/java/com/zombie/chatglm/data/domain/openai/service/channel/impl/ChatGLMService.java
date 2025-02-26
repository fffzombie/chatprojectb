package com.zombie.chatglm.data.domain.openai.service.channel.impl;

import com.alibaba.fastjson.JSON;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.service.channel.OpenAiGroupService;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import com.zombie.chatglm.model.ChatCompletionRequest;
import com.zombie.chatglm.model.ChatCompletionResponse;
import com.zombie.chatglm.model.EventType;
import com.zombie.chatglm.model.Model;
import com.zombie.chatglm.session.OpenAiSession;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired(required = false)
    protected OpenAiSession chatGLMOpenAiSession;

    private boolean isEmitterCompleted = false;  // 用于标记 emitter 是否已完成

    @Timed(value = "chatglmmodel_domessage_response", description = "chatglm模型对话次数")
    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcessAggregate, ResponseBodyEmitter emitter,StringBuilder fullResponse) throws Exception {
        //1.请求消息
        List<ChatCompletionRequest.Prompt> messages = chatProcessAggregate.getMessages().stream()
                .map(sessionMessageVO -> ChatCompletionRequest.Prompt.builder()
                        .role(sessionMessageVO.getRole().getCode())
                        .content(sessionMessageVO.getContent())
                        .build())
                .collect(Collectors.toList());


        //2.封装参数
        ChatCompletionRequest request = new ChatCompletionRequest();
        //TODO:没有GPT时写死模型
//        request.setModel(Model.getModelByCode(chatProcessAggregate.getModel()));
        request.setModel(Model.GLM_4_FLASH);
        request.setPrompt(messages);

        //3.请求应答
        chatGLMOpenAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                if(isEmitterCompleted){
                    return;
                }
                //发送信息
                try {
                    if (response.getData() != null && EventType.add.getCode().equals(type)) {
                        emitter.send(response.getData());
                        fullResponse.append(response.getData());
                    }
                } catch (IOException e) {
                    //用户中断会话时仅记录简单信息
                    if (e.getMessage().contains("你的主机中的软件中止了一个已建立的连接。")){
                        log.info("流式问答请求已被用户中止: {}", e.getMessage());
                    }else{
                        // 对于其他异常记录详细信息
                        log.error(e.getMessage());
                        throw new ChatGLMException(e.getMessage());
                    }
                }
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                emitter.complete();
                log.info("对话关闭");
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                if (t instanceof IOException) {
                    // 仅记录连接中断的情况，避免过多的错误日志
                    emitter.complete();
                    log.info("流式问答请求中止，连接中断: {}", t.getMessage());
                } else {
                    // 其他类型的异常记录完整的堆栈
                    emitter.complete();
                    log.error("对话异常", t);
                }
            }
        });


    }
}
