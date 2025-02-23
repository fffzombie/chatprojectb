package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.UserAccountStatusVO;
import com.zombie.chatglm.data.domain.openai.repository.IOpenAiRepository;
import com.zombie.chatglm.data.infrastructure.dao.IUserAccountDao;
import com.zombie.chatglm.data.infrastructure.po.mysql.UserAccountPO;
import com.zombie.chatglm.data.infrastructure.redis.IRedisService;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @description OpenAi 仓储服务
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private IRedisService redisService;

    private static final String REDIS_USER_FREE_PREFIX = "user:freecount:";

    @Override
    public UserAccountQuotaEntity queryUserAccount(String openid) {

        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return null;
        UserAccountQuotaEntity userAccountQuotaEntity = new UserAccountQuotaEntity();
        userAccountQuotaEntity.setOpenid(userAccountPO.getOpenid());
        userAccountQuotaEntity.setUserAccountStatusVO(UserAccountStatusVO.get(userAccountPO.getStatus()));
        userAccountQuotaEntity.setSurplusQuota(userAccountPO.getSurplusQuota());
        userAccountQuotaEntity.setTotalQuota(userAccountPO.getTotalQuota());
        userAccountQuotaEntity.genModelTypes(userAccountPO.getModelTypes());

        return userAccountQuotaEntity;
    }

    @Override
    public int subAccountQuota(String openid) {

        return userAccountDao.subAccountQuota(openid);


    }

    @Override
    public Integer queryFreeCount(String openid) {
        String key = REDIS_USER_FREE_PREFIX + openid;
        return redisService.<Integer>getValue(key);
    }

    @Override
    public void setUserFreeCount(String openid, Integer count) {
        redisService.setIfAbsent(REDIS_USER_FREE_PREFIX + openid, count, 24 * 60 * 60 * 1000);
    }

    @Override
    public void subUserFreeCount(String openid) {
        Integer count = redisService.<Integer>getValue(REDIS_USER_FREE_PREFIX + openid);
        if (count > 0) {
//            redisService.setValue(REDIS_USER_FREE_PREFIX + openid,count - 1);
            redisService.decr(REDIS_USER_FREE_PREFIX + openid);
        }
    }
}
