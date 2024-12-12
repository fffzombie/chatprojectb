package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.auth.repository.IAuthRepository;
import com.zombie.chatglm.data.infrastructure.redis.IRedisService;
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


    private static final String Key = "weixin_code";
    @Resource
    private IRedisService redisService;
    @Override
    public String getOpenIdByCode(String code) {
        return redisService.getValue(Key + "_" + code);
    }

    @Override
    public void removeCodeByOpenId(String code, String openId) {
        redisService.remove(Key + "_" + code);
        redisService.remove(Key + "_" + openId);
    }
}
