package com.zombie.chatglm.data.domain.openai.repository;


import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionHeaderEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;

import java.util.List;

/**
 * @description OpenAi 仓储接口
 */
public interface IOpenAiRepository {
    UserAccountQuotaEntity queryUserAccount(String openid);

    int subAccountQuota(String openid);

    Integer queryFreeCount(String openid);

    void setUserFreeCount(String openid,Integer count);

    void subUserFreeCount(String openid);

    void updateSessionMessage(SessionMessageVO messageVO, ChatProcessAggregate chatProcess);

    List<SessionHeaderEntity> queryMessageHeaders(String openid);

    List<SessionMessageVO> querySessionMessages(String openid, String sessionId);

    void insertSession(SessionEntity sessionEntity);

    boolean validateSession(String sessionId, String openid);

    void deleteSession(String openid, String sessionId);

    void changeSessionTitle(String openid, String sessionId, String newTitle);
}
