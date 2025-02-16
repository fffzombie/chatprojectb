package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.auth.repository.IAuthRepository;
import com.zombie.chatglm.data.domain.openai.model.valobj.UserAccountStatusVO;
import com.zombie.chatglm.data.infrastructure.dao.IUserAccountDao;
import com.zombie.chatglm.data.infrastructure.po.UserAccountPO;
import com.zombie.chatglm.data.infrastructure.redis.IRedisService;
import com.zombie.chatglm.data.types.enums.OpenAIUserEnableModelTypes;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * ClassName: AuthRepository
 * Package: com.zombie.chatglm.data.infrastructure.repository
 * Description:
 *
 * @Author ME
 * @Create 2024/10/7 17:59
 * @Version 1.0
 */
@Repository
public class AuthRepository implements IAuthRepository {


    private static final String Key = "weixin:code:";
    @Resource
    private IRedisService redisService;
    @Resource
    private IUserAccountDao userAccountDao;

    @Override
    public String getOpenIdByCode(String code) {
        return redisService.getValue(Key + code);
    }

    @Override
    public void removeCodeByOpenId(String code, String openId) {
        redisService.remove(Key + code);
        redisService.remove(Key + openId);
    }

    @Override
    public void registerAccount(String openId) {
        UserAccountPO userAccount = userAccountDao.queryUserAccount(openId);
        if (null == userAccount) {
            UserAccountPO userAccountPO = UserAccountPO.builder()
                    .openid(openId)
                    .modelTypes(OpenAIUserEnableModelTypes.ALL.getCode())
                    .status(UserAccountStatusVO.AVAILABLE.getCode())
                    .surplusQuota(0)
                    .totalQuota(0)
                    .build();
            userAccountDao.insert(userAccountPO);
        }
    }
}
