package com.zombie.chatglm.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName: ChatGPTSDKConfigProperties
 * Package: com.zombie.chatglm.data.config
 * Description:
 *
 * @Author ME
 * @Create 2024/10/5 16:44
 * @Version 1.0
 */
@Data
@ConfigurationProperties(prefix = "chatgpt.sdk.config",ignoreInvalidFields = true)
public class ChatGPTSDKConfigProperties {
    /**  转发地址   */
    private String apiHost;
    /**   请求秘钥      */
    private String apiKey;
}
