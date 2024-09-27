package com.zombie.chatglm.data.domain.auth.service;

import com.zombie.chatglm.data.domain.auth.model.entity.AuthStateEntity;

public interface IAuthService {
    AuthStateEntity doLogin(String code);

    boolean checkToken(String token);
    String openid(String token);
}
