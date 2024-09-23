package com.zombie.chatglm.data.domain.weixin.service.validate;

import com.zombie.chatglm.data.domain.weixin.service.IWeiXinValidateService;
import com.zombie.chatglm.data.types.sdk.weixin.SignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/***
 *
 * @Description 验签接口实现
 * @Date 17:39 2024/9/21
 *
 */
@Service
public class WeiXinValidateService implements IWeiXinValidateService {
    @Value("${wx.config.token}")
    private String token;
    @Override
    public boolean checkSign(String signature, String timestamp, String nonce) {
        return SignatureUtil.check(token,signature,timestamp,nonce);
    }
}
