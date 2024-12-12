package com.zombie.chatglm.data.domain.auth.service;

import com.google.common.cache.Cache;
import com.zombie.chatglm.data.domain.auth.model.entity.AuthStateEntity;
import com.zombie.chatglm.data.domain.auth.model.valobj.AuthTypeVO;
import com.zombie.chatglm.data.domain.auth.repository.IAuthRepository;
import io.jsonwebtoken.Claims;
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

    //原功能替换为redis，已废弃
//    @Resource
//    private Cache<String,String> codeCache;

    @Resource
    private IAuthRepository authRepository;


    @Override
    protected AuthStateEntity checkCode(String code) {
        //通过缓存获取验证码校验
        String openId = authRepository.getOpenIdByCode(code);
        if(StringUtils.isAnyBlank(openId)){
            log.info("鉴权，用户输入的验证码不存在 {}", code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVO.A0001.getCode())
                    .info(AuthTypeVO.A0001.getInfo())
                    .build();
        }

        //移除key值
        authRepository.removeCodeByOpenId(code,openId);


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

    @Override
    public String openid(String token) {
        Claims claims = decode(token);
        return claims.get("openId").toString();
    }
}
