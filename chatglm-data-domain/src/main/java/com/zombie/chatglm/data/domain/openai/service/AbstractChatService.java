package com.zombie.chatglm.data.domain.openai.service;

import cn.bugstack.chatglm.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;

/*
*  定义业务流程
*
* */
@Slf4j
public abstract class AbstractChatService implements IChatService{

    @Resource
    protected OpenAiSession openAiSession;

    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess) {
        //1.检验权限
        if(!"b8b6".equals(chatProcess.getToken())){
            throw new ChatGLMException(Constants.ResponseCode.TOKEN_ERROR.getCode(),Constants.ResponseCode.TOKEN_ERROR.getInfo());
        }

        //2.请求应答
//        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型：{}",chatProcess.getModel());
        });

        emitter.onError(throwable -> log.error("流式问答请求异常，使用模型：{}", chatProcess.getModel(), throwable));


        //3.应答处理
        try{
            this.doMessageResponse(chatProcess,emitter);
        }catch (Exception e){
            throw new ChatGLMException(Constants.ResponseCode.UN_ERROR.getCode(),Constants.ResponseCode.UN_ERROR.getInfo());
        }
        return emitter;
    }

    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws Exception;
}
