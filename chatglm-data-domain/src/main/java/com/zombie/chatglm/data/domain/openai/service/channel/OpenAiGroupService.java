package com.zombie.chatglm.data.domain.openai.service.channel;

import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * ClassName: OpenAiGroupService
 * Package: com.zombie.chatglm.data.domain.openai.service.channel
 * Description:封装不同模型的请求过程
 * @Create 2024/10/5 16:37
 * @Version 1.0
 */
public interface OpenAiGroupService {
    void doMessageResponse(ChatProcessAggregate chatProcessAggregate, ResponseBodyEmitter emitter) throws Exception;
}
