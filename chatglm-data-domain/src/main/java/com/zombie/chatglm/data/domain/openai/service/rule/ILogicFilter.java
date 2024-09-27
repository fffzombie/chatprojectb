package com.zombie.chatglm.data.domain.openai.service.rule;

import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.RuleLogicEntity;

/**
 * @description 规则过滤接口
 */
public interface ILogicFilter {

    RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess) throws Exception;

}
