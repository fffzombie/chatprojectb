package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.weixin.repository.IWeiXinRepository;
import com.zombie.chatglm.data.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: WeiXinRepository
 * Package: com.zombie.chatglm.data.infrastructure.repository
 * Description:
 *
 * @Author ME
 * @Create 2024/10/7 17:28
 * @Version 1.0
 */
@Slf4j
@Repository
public class WeiXinRepository implements IWeiXinRepository {

    @Resource
    private IRedisService redisService;

    private static final String Key = "weixin:code:";


    @Override
    public String genCode(String openId) {
        String existsCode = redisService.getValue(Key + openId);
        if (StringUtils.isNotBlank(existsCode)) return existsCode;
        //生成值
        RLock lock = redisService.getLock(Key);//加锁防止读写脏数据
        try {
            lock.lock(15, TimeUnit.SECONDS);

            String code = RandomStringUtils.randomNumeric(4);
            // 防重校验&重新生成
            for (int i = 0; i < 10 && StringUtils.isNotBlank(redisService.getValue(Key + code)); i++) {
                if (i < 3) {
                    code = RandomStringUtils.randomNumeric(4);
                } else if (i < 5) {
                    code = RandomStringUtils.randomNumeric(5);
                } else if (i < 9) {
                    code = RandomStringUtils.randomNumeric(6);
                    log.warn("验证码重复，生成6位字符串验证码 {} {}", openId, code);
                } else {
                    return "您的验证码获取失败，请重新回复405获取。";
                }
            }
            //存储值，三分钟有效期
            redisService.setValue(Key + code, openId, 3 * 60 * 1000);
            redisService.setValue(Key + openId, code, 3 * 60 * 1000);

            return code;

        } finally {
            lock.unlock();
        }
    }
}
