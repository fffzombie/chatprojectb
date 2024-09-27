package com.zombie.chatglm.data.domain.openai.service;
/*
*
* 业务具体实现
* */

import cn.bugstack.chatglm.model.*;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.RuleLogicEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.LogicCheckTypeVO;
import com.zombie.chatglm.data.domain.openai.service.rule.ILogicFilter;
import com.zombie.chatglm.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService extends AbstractChatService{

    @Resource
    DefaultLogicFactory logicFactory;

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
        request.setModel(Model.getModelByCode(chatProcess.getModel()));
        request.setPrompt(messages);

        //3.请求应答
        openAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                ChatCompletionResponse response = JSON.parseObject(data,ChatCompletionResponse.class);

                //应答完成
                if("[DONE]".equals(data)){
                    log.info("[输出结束] Tokens {}", JSON.toJSONString(data));
                    responseBodyEmitter.complete();
                }

                //发送信息
                try {
                    responseBodyEmitter.send(response.getData());
                } catch (IOException e) {
                    throw new ChatGLMException(e.getMessage());
                }


            }

        });


    }

    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, String... logics) throws Exception {
        Map<String, ILogicFilter> logicFilterMap = logicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        for (String code : logics) {
            //拿到对应的过滤器实现类调用filter方法
            entity = logicFilterMap.get(code).filter(chatProcess);
            if(!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) return entity;
        }
        return entity != null ? entity : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
    }
}
