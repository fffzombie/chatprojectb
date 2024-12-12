package com.zombie.chatglm.data.domain.auth.repository;

/**
 * ClassName: IAuthRepository
 * Package: com.zombie.chatglm.data.domain.auth.repository
 * Description: 认证仓储服务
 *
 * @Author ME
 * @Create 2024/10/7 17:57
 * @Version 1.0
 */
public interface IAuthRepository {
    String getOpenIdByCode(String code);

    void removeCodeByOpenId(String code,String openId);

}
