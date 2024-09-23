package com.zombie.chatglm.data.domain.auth.service;

import com.google.common.cache.Cache;
import com.zombie.chatglm.data.domain.auth.model.entity.AuthStateEntity;
import com.zombie.chatglm.data.domain.auth.model.valobj.AuthTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @description 验证码鉴权服务具体实现
 */
@Slf4j
@Service
public class AuthService extends AbstractAuthService{

    @Resource
    private Cache<String,String> codeCache;


    @Override
    protected AuthStateEntity checkCode(String code) {
        //通过缓存获取验证码校验
        String openId = codeCache.getIfPresent(code);
        if(StringUtils.isAnyBlank(openId)){
            log.info("鉴权，用户输入的验证码不存在 {}", code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVO.A0001.getCode())
                    .info(AuthTypeVO.A0001.getInfo())
                    .build();
        }

        //移除缓存key值
        codeCache.invalidate(openId);
        codeCache.invalidate(code);


        return AuthStateEntity.builder()
                .code(AuthTypeVO.A0000.getCode())
                .info(AuthTypeVO.A0000.getInfo())
                .openId(openId)
                .build();
    }


    @Override
    public boolean checkToken(String token) {
        return isVerify(token);
    }
}
