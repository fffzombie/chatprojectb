package com.zombie.chatglm.data.domain.openai.repository;


import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;

/**
 * @description OpenAi 仓储接口
 */
public interface IOpenAiRepository {
    UserAccountQuotaEntity queryUserAccount(String openid);

    int subAccountQuota(String openid);

    Integer queryFreeCount(String openid);

    void setUserFreeCount(String openid,Integer count);

    void subUserFreeCount(String openid);
}
