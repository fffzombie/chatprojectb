package com.zombie.chatglm.data.domain.openai.service;

import com.zombie.chatglm.data.domain.openai.model.event.ChatMessageEvent;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.domain.openai.service.channel.OpenAiGroupService;
import com.zombie.chatglm.data.domain.openai.service.channel.impl.ChatGLMService;
import com.zombie.chatglm.data.domain.openai.service.channel.impl.ChatGPTService;
import com.zombie.chatglm.data.types.enums.ChatMessageRole;
import com.zombie.chatglm.data.types.enums.OpenAiChannel;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.RuleLogicEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.LogicCheckTypeVO;
import com.zombie.chatglm.data.domain.openai.repository.IOpenAiRepository;
import com.zombie.chatglm.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
 *  定义标准业务方法
 *
 * */
@Slf4j
public abstract class AbstractChatService implements IChatService {
    @Resource
    protected IOpenAiRepository openAiRepository;


    private final Map<OpenAiChannel, OpenAiGroupService> openAiGroup = new HashMap<>();

    public AbstractChatService(ChatGPTService chatGPTService, ChatGLMService chatGLMService) {
        openAiGroup.put(OpenAiChannel.ChatGPT, chatGPTService);
        openAiGroup.put(OpenAiChannel.ChatGLM, chatGLMService);
    }

    @Override
    public ResponseBodyEmitter completions(ResponseBodyEmitter emitter, ChatProcessAggregate chatProcess) {
        try {
            // 创建消息收集容器
            StringBuilder fullResponse = new StringBuilder();
            long respSendTime = System.currentTimeMillis();
            //1.请求应答
            emitter.onCompletion(() -> {
                try {
                    appendMessageToSession(ChatMessageEvent.builder()
                            .sessionMessageVO(SessionMessageVO.builder()
                                    .role(ChatMessageRole.AI)
                                    .content(fullResponse.toString())
                                    .sendTime(respSendTime)
                                    .build())
                            .chatProcess(chatProcess)
                            .build());
                } catch (Exception e) {
                    log.error("写入AIResp错误" + e.getMessage());
                }
                log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
            });
            emitter.onError(throwable -> {
                // 判断是否是业务的连接中断异常
                if (throwable instanceof IOException) {
                    String message = throwable.getMessage();
                    // 根据错误消息判断是否为业务的连接问题
                    if (message != null && (message.contains("你的主机中的软件中止了一个已建立的连接。"))) {
                        // 连接中断的异常，记录简短信息
                        log.info("流式问答请求被用户中止，使用模型：{}，连接中断: {}", chatProcess.getModel(), message);
                    } else {
                        // 对于其他类型的 IO 异常，记录详细信息
                        log.error("流式问答请求中止，使用模型：{}，发生 IOException: {}", chatProcess.getModel(), message, throwable);
                    }
                } else {
                    // 对于其他异常类型，记录完整的堆栈信息
                    log.error("流式问答请求中止，使用模型：{}，发生异常: {}", chatProcess.getModel(), throwable.getMessage(), throwable);
                }
            });

            //2.账户查询
            UserAccountQuotaEntity userAccountQuotaEntity = openAiRepository.queryUserAccount(chatProcess.getOpenid());

            //3.规则过滤
            RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess, userAccountQuotaEntity,
//                    DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                    DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode(),
                    DefaultLogicFactory.LogicModel.ACCOUNT_STATUS.getCode(),
                    DefaultLogicFactory.LogicModel.MODEL_TYPE.getCode(),
                    DefaultLogicFactory.LogicModel.USER_QUOTA.getCode()
            );
            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
                emitter.send(ruleLogicEntity.getInfo());
                try {
                    appendMessageToSession(ChatMessageEvent.builder()
                            .sessionMessageVO(SessionMessageVO.builder()
                                    .role(ChatMessageRole.AI)
                                    .content(ruleLogicEntity.getInfo())
                                    .sendTime(respSendTime)
                                    .build())
                            .chatProcess(chatProcess)
                            .build());
                } catch (Exception e) {
                    log.error("写入AIResp错误" + e.getMessage());
                }
                log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
                emitter.complete();
                return emitter;
            }
            //4.应答处理 【策略模式】
            openAiGroup.get(chatProcess.getChannel()).doMessageResponse(chatProcess, emitter, fullResponse);
        } catch (Exception e) {
            throw new ChatGLMException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        //5.返回结果
        return emitter;
    }

    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception;
}
