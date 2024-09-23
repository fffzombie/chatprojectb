package com.zombie.chatglm.data.domain.weixin.service;

import com.zombie.chatglm.data.domain.weixin.model.entity.UserBehaviorMessageEntity;

public interface IWeiXinBehaviorService {

    String acceptUserBehavior(UserBehaviorMessageEntity entity);

}
