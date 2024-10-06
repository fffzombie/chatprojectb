package com.zombie.chatglm.data.config;

import com.zombie.chatgpt.session.OpenAiSession;
import com.zombie.chatgpt.session.OpenAiSessionFactory;
import com.zombie.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: ChatGPTSDKConfig
 * Package: com.zombie.chatglm.data.config
 * Description: 工厂配置
 *
 * @Author ME
 * @Create 2024/10/5 16:44
 * @Version 1.0
 */
@Configuration
@EnableConfigurationProperties(ChatGPTSDKConfigProperties.class)
public class ChatGPTSDKConfig {
    @Bean(name = "chatGPTOpenAiSession")
    public OpenAiSession openAiSession(ChatGPTSDKConfigProperties properties){
        //1.配置文件
        com.zombie.chatgpt.session.Configuration configuration = new com.zombie.chatgpt.session.Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiKey(properties.getApiKey());

        //2.会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

        //3.开启会话
        return factory.openSession();
    }
}
