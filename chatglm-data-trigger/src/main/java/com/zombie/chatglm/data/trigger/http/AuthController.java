package com.zombie.chatglm.data.trigger.http;

import com.zombie.chatglm.data.domain.auth.model.entity.AuthStateEntity;
import com.zombie.chatglm.data.domain.auth.model.valobj.AuthTypeVO;
import com.zombie.chatglm.data.domain.auth.service.IAuthService;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description 鉴权登录
 */
@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/auth/")
public class AuthController {

    @Resource
    private IAuthService authService;

    @RequestMapping(value = "login",method = RequestMethod.POST)
    public Response<String> doLogin(@RequestParam String code){
        log.info("鉴权登录校验开始，验证码: {}", code);
        try {
            AuthStateEntity authStateEntity = authService.doLogin(code);
            //鉴权失败，拦截
            if(!AuthTypeVO.A0000.getCode().equals(authStateEntity.getCode())){
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            //鉴权成功，发放token
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(authStateEntity.getToken())
                    .build();
        }catch (Exception e){
            log.error("鉴权登录校验失败，验证码: {}", code);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }


}

