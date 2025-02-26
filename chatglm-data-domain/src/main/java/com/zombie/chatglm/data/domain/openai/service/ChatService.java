package com.zombie.chatglm.data.domain.openai.service;
/*
 *
 * 业务具体实现
 * */

import cn.hutool.core.util.IdUtil;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.RuleLogicEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.event.ChatMessageEvent;
import com.zombie.chatglm.data.domain.openai.model.valobj.LogicCheckTypeVO;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionHeaderEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.domain.openai.repository.mq.IChatMQProducer;
import com.zombie.chatglm.data.domain.openai.service.channel.impl.ChatGLMService;
import com.zombie.chatglm.data.domain.openai.service.channel.impl.ChatGPTService;
import com.zombie.chatglm.data.domain.openai.service.rule.ILogicFilter;
import com.zombie.chatglm.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatService extends AbstractChatService {

    @Resource
    private DefaultLogicFactory logicFactory;

    @Resource
    private IChatMQProducer chatMQProducer;

    public ChatService(ChatGPTService chatGPTService, ChatGLMService chatGLMService) {
        super(chatGPTService, chatGLMService);
    }

    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception {
        Map<String, ILogicFilter<UserAccountQuotaEntity>> logicFilterMap = logicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        for (String code : logics) {
            //如果传入空过滤则此次不调用
            if (DefaultLogicFactory.LogicModel.NULL.getCode().equals(code)) continue;
            //拿到对应的过滤器实现类调用filter方法
            entity = logicFilterMap.get(code).filter(chatProcess, userAccountQuotaEntity);
            if (!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) return entity;
        }
        return entity != null ? entity : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
    }

    @Override
    public List<SessionHeaderEntity> querySessionHeaders(String openid) {
        return openAiRepository.queryMessageHeaders(openid);
    }

    @Override
    public List<SessionMessageVO> querySessionMessages(String openid, String sessionId) {
        return openAiRepository.querySessionMessages(openid, sessionId);
    }

    @Override
    public String createSession(SessionEntity sessionEntity) {

        String sessionId = "chat_" + IdUtil.getSnowflake().nextIdStr();

        sessionEntity.setSessionId(sessionId);

        openAiRepository.insertSession(sessionEntity);

        return sessionId;
    }

    @Override
    public boolean validateSession(String sessionId, String openid) {
        return openAiRepository.validateSession(sessionId, openid);
    }

    @Override
    public void deleteSession(String openid, String sessionId) {
        openAiRepository.deleteSession(openid, sessionId);
    }

    @Override
    public void changeSessionTitle(String openid, String sessionId, String newTitle) {
        openAiRepository.changeSessionTitle(openid, sessionId, newTitle);
    }

    public void appendMessageToSession(ChatMessageEvent messageEvent) {
        chatMQProducer.sendUpdateSessionMessage(messageEvent);
    }
}
