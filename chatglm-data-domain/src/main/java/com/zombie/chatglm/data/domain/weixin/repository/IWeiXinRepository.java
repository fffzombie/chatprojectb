package com.zombie.chatglm.data.domain.weixin.repository;

/**
 * ClassName: IWeiXinRepository
 * Package: com.zombie.chatglm.data.domain.weixin.repository
 * Description:微信服务仓储
 *
 * @Author ME
 * @Create 2024/10/7 17:24
 * @Version 1.0
 */
public interface IWeiXinRepository {
    String genCode(String openId);
}
