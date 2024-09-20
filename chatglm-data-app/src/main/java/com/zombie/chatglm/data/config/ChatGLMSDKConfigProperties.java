package com.zombie.chatglm.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatglm.sdk.config",ignoreInvalidFields = true)
public class ChatGLMSDKConfigProperties {
    //chatglm官网地址
    private String apiHost;
    //秘钥
    private String apiKey;
}
