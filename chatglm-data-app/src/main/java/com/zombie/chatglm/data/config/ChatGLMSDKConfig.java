package com.zombie.chatglm.data.config;

import com.zombie.chatglm.session.OpenAiSession;
import com.zombie.chatglm.session.OpenAiSessionFactory;
import com.zombie.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 *
 * @Description 配置会话工厂
 * @Date 15:56 2024/9/19
 *
 *
 */


@Configuration
@EnableConfigurationProperties(ChatGLMSDKConfigProperties.class)
public class ChatGLMSDKConfig {
    @Bean(name = "chatGLMOpenAiSession")
    @ConditionalOnProperty(value = "chatglm.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public OpenAiSession openAiSession(ChatGLMSDKConfigProperties properties){
        //1.配置文件
        com.zombie.chatglm.session.Configuration configuration = new com.zombie.chatglm.session.Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiSecretKey(properties.getApiKey());

        //2.会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

        //3.开启会话
        return factory.openSession();
    }
}
