package com.zombie.chatglm.data.domain.openai.service.rule.impl;

import com.zombie.chatglm.data.domain.openai.annotation.LogicStrategy;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.RuleLogicEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.LogicCheckTypeVO;
import com.zombie.chatglm.data.domain.openai.model.valobj.UserAccountStatusVO;
import com.zombie.chatglm.data.domain.openai.service.rule.ILogicFilter;
import com.zombie.chatglm.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCOUNT_STATUS)
public class AccountStatusFilter implements ILogicFilter<UserAccountQuotaEntity> {
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        //账户可用，直接放行
        if(UserAccountStatusVO.AVAILABLE.equals(data.getUserAccountStatusVO())){
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS)
                    .data(chatProcess)
                    .build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.REFUSE)
                .data(chatProcess)
                .info("您的账户已冻结，暂时不可使用。如果有疑问，可以联系客户解冻账户。")
                .build();
    }
}
