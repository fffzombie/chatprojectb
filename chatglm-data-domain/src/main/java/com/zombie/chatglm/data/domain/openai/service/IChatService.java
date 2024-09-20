package com.zombie.chatglm.data.domain.openai.service;

import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;


/***
 *
 * @Description 处理ai请求的接口
 * @Date 22:54 2024/9/19
 * @return
 *
 */
public interface IChatService {
    ResponseBodyEmitter completions(ChatProcessAggregate chatProcess);
}
