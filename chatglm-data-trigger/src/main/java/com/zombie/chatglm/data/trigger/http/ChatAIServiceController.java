package com.zombie.chatglm.data.trigger.http;


import com.alibaba.fastjson.JSON;
import com.zombie.chatglm.data.domain.auth.service.IAuthService;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.MessageEntity;
import com.zombie.chatglm.data.domain.openai.service.IChatService;
import com.zombie.chatglm.data.trigger.http.dto.ChatGLMRequestDTO;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/")
public class ChatAIServiceController {

    @Resource
    private IChatService chatService;

    @Resource
    private IAuthService authService;

    /**
     * 流式问题，ChatGPT 请求接口
     * <p>
     * curl -X POST \
     * http://localhost:8090/api/v1/chat/completions \
     * -H "Content-Type: application/json;charset=utf-8" \
     * -H "Authorization: b8b6" \
     * -d "{
     * "messages": [
     * {
     * "content": "写一个java冒泡排序",
     * "role": "user"
     * }
     * ],
     * "model": "glm-3-turbo"
     * }"
     */
    @Timed(value = "chat_completions",description = "对话请求量")
    @RequestMapping(value = "chat/completions",method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGLMRequestDTO request,
                                                 @RequestHeader("Authorization") String token,
                                                 HttpServletResponse response){
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));
        try{
            // 1. 基础配置；流式输出、编码、禁用缓存
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 2. 构建异步响应对象【对 Token 过期拦截】
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
            boolean success = authService.checkToken(token);
            if(!success){
                try {
                    emitter.send(Constants.ResponseCode.TOKEN_ERROR.getCode());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                emitter.complete();
                return emitter;
            }

            //3.获取openid
            String openid = authService.openid(token);
            log.info("流式问答请求处理，openid:{} 请求模型:{}", openid, request.getModel());

            //4.构建参数
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(request.getMessages().stream()
                            .map(entity -> MessageEntity.builder()
                                    .role(entity.getRole())
                                    .content(entity.getContent())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            //5.请求结果&返回
            return chatService.completions(emitter,chatProcessAggregate);

        }catch (Exception e) {
            log.error("流式应答，请求模型：{} 发生异常", request.getModel(), e);
            throw new ChatGLMException(e.getMessage());
        }

    }

}
