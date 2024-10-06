package com.zombie.chatglm.data.domain.openai.service;
/*
*
* 业务具体实现
* */

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.RuleLogicEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.LogicCheckTypeVO;
import com.zombie.chatglm.data.domain.openai.service.channel.impl.ChatGLMService;
import com.zombie.chatglm.data.domain.openai.service.channel.impl.ChatGPTService;
import com.zombie.chatglm.data.domain.openai.service.rule.ILogicFilter;
import com.zombie.chatglm.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import com.zombie.chatglm.model.ChatCompletionRequest;
import com.zombie.chatglm.model.ChatCompletionResponse;
import com.zombie.chatglm.model.Model;
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
    private DefaultLogicFactory logicFactory;

    public ChatService(ChatGPTService chatGPTService, ChatGLMService chatGLMService) {
        super(chatGPTService, chatGLMService);
    }

    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception {
        Map<String, ILogicFilter<UserAccountQuotaEntity>> logicFilterMap = logicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        for (String code : logics) {
            //如果传入空过滤则此次不调用
            if(DefaultLogicFactory.LogicModel.NULL.getCode().equals(code))continue;
            //拿到对应的过滤器实现类调用filter方法
            entity = logicFilterMap.get(code).filter(chatProcess,userAccountQuotaEntity);
            if(!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) return entity;
        }
        return entity != null ? entity : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
    }
}
