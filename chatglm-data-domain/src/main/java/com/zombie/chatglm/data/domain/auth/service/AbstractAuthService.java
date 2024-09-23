package com.zombie.chatglm.data.domain.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.zombie.chatglm.data.domain.auth.model.entity.AuthStateEntity;
import com.zombie.chatglm.data.domain.auth.model.valobj.AuthTypeVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.util.*;

/**
 * @description 鉴权服务业务流程
 */

@Slf4j
public abstract class AbstractAuthService implements IAuthService{

    private static final String defaultBase64EncodedSecretKey = "A*U*J^S3c";
    private final String base64EncodedSecretKey = Base64.encodeBase64String(defaultBase64EncodedSecretKey.getBytes());
    private final Algorithm algorithm = Algorithm.HMAC256(Base64.decodeBase64(Base64.encodeBase64String(defaultBase64EncodedSecretKey.getBytes())));

    @Override
    public AuthStateEntity doLogin(String code) {
        //1.不是4位数字直接返回失败
        if(!code.matches("\\d{4}")){
            log.info("鉴权，用户收入的验证码无效 {}", code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVO.A0002.getCode())
                    .info(AuthTypeVO.A0002.getInfo())
                    .build();
        }

        //2.检验判断
        AuthStateEntity authStateEntity = this.checkCode(code);
        if(!authStateEntity.getCode().equals(AuthTypeVO.A0000.getCode())){
            return authStateEntity;
        }

        //3.获取token并返回
        Map<String, Object> chaim = new HashMap<>();
        chaim.put("openId",authStateEntity.getOpenId());
        String token = encode(authStateEntity.getOpenId(),7 * 24 * 60 * 60 * 1000,chaim);
        authStateEntity.setToken(token);
        return authStateEntity;
    }

    protected abstract AuthStateEntity checkCode(String code);

    /**
     * 这里就是产生jwt字符串的地方
     * jwt字符串包括三个部分
     * 1. header
     * -当前字符串的类型，一般都是“JWT”
     * -哪种算法加密，“HS256”或者其他的加密算法
     * 所以一般都是固定的，没有什么变化
     * 2. payload
     * 一般有四个最常见的标准字段（下面有）
     * iat：签发时间，也就是这个jwt什么时候生成的
     * jti：JWT的唯一标识
     * iss：签发人，一般都是username或者userId
     * exp：过期时间
     */
    protected String encode(String issuer, long ttlMillis, Map<String, Object> claims){
        // iss签发人，ttlMillis生存时间，claims是指还想要在jwt中存储的一些非隐私信息
        if(claims == null){
            claims = new HashMap<>();
        }

        long nowMillis = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                //荷载部分
                .setClaims(claims)
                //Jwt唯一标识
                .setId(UUID.randomUUID().toString())
                //签发时间
                .setIssuedAt(new Date(nowMillis))
                //签发人
                .setSubject(issuer)
                //使用的算法和秘钥
                .signWith(SignatureAlgorithm.HS256,base64EncodedSecretKey);
        if(ttlMillis >= 0){
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            //设置过期时间
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    //获取荷载部分所有的键值对 Claims为map
    protected Claims decode(String jwtToken) {
        // 得到 DefaultJwtParser
        return Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(base64EncodedSecretKey)
                // 设置需要解析的 jwt
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    // 判断jwtToken是否合法
    protected boolean isVerify(String jwtToken) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(jwtToken);
            // 校验不通过会抛出异常
            // 判断合法的标准：1. 头部和荷载部分没有篡改过。2. 没有过期
            return true;
        } catch (Exception e) {
            log.error("jwt isVerify Err", e);
            return false;
        }

    }




}
