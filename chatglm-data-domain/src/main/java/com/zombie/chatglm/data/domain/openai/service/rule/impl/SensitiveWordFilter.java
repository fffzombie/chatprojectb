package com.zombie.chatglm.data.domain.openai.service.rule.impl;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.zombie.chatglm.data.domain.openai.annotation.LogicStrategy;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.RuleLogicEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.LogicCheckTypeVO;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.domain.openai.service.rule.ILogicFilter;
import com.zombie.chatglm.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @description 敏感词过滤
 */

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.SENSITIVE_WORD)
public class SensitiveWordFilter implements ILogicFilter<UserAccountQuotaEntity> {
    @Value("${app.config.white-list}")
    private String whiteListStr;

    @Resource
    private SensitiveWordBs words;
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess,UserAccountQuotaEntity data) throws Exception {
        //白名单用户不做敏感词处理
        if(chatProcess.isWhiteList(whiteListStr)){
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        ChatProcessAggregate newChatProcessAggregate = new ChatProcessAggregate();
        newChatProcessAggregate.setOpenid(chatProcess.getOpenid());
        newChatProcessAggregate.setModel(chatProcess.getModel());

        List<SessionMessageVO> newMessages = chatProcess.getMessages().stream()
                .map(message -> {
                    String content = message.getContent();
                    String replace = words.replace(content);
                    Long sendTime = message.getSendTime();
                    return SessionMessageVO.builder()
                            .role(message.getRole())
                            .content(replace)
                            .sendTime(sendTime)
                            .build();
                })
                .collect(Collectors.toList());
        newChatProcessAggregate.setMessages(newMessages);


        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(newChatProcessAggregate)
                .build();
    }
}
