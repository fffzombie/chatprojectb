package com.zombie.chatglm.data.trigger.http;


import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.zombie.chatglm.data.domain.auth.service.IAuthService;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionEntity;
import com.zombie.chatglm.data.domain.openai.model.event.ChatMessageEvent;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionConfigVO;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionHeaderEntity;
import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.domain.openai.service.IChatService;
import com.zombie.chatglm.data.trigger.http.dto.ChangeSessionTitleRequestDTO;
import com.zombie.chatglm.data.trigger.http.dto.ChatAIRequestDTO;
import com.zombie.chatglm.data.trigger.http.dto.QuestMessageDTO;
import com.zombie.chatglm.data.trigger.http.dto.SessionHeaderDTO;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.enums.ChatMessageRole;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import com.zombie.chatglm.data.types.model.Response;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(value = "${app.config.cross-origin}",exposedHeaders = "X-Session-Id")
@RequestMapping("/api/${app.config.api-version}/chat/")
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
    @Timed(value = "chat_completions", description = "对话请求量")
    @RequestMapping(value = "completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatAIRequestDTO chatRequest,
                                                 @RequestHeader("Authorization") String token,
                                                 HttpServletResponse response) {
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", chatRequest.getModel(), JSON.toJSONString(chatRequest.getMessages()));
        try {
            // 1. 基础配置；流式输出、编码、禁用缓存
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 2. 构建异步响应对象【对 Token 过期拦截】
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
            boolean success = authService.checkToken(token);
            if (!success) {
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
            log.info("流式问答请求处理，openid:{} 请求模型:{}", openid, chatRequest.getModel());

            //4.参数声明
            ChatProcessAggregate chatProcessAggregate = null;

            //5.会话处理
            String sessionId = chatRequest.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                log.info("创建会话....");
                //创建会话
                SessionEntity sessionEntity = SessionEntity.builder()
                        .sessionTitle(chatRequest.getMessages().get(0).getContent())
                        .messageList(chatRequest.getMessages().stream()
                                .map(dto -> SessionMessageVO.builder()
                                        .content(dto.getContent())
                                        .role(ChatMessageRole.getByCode(dto.getRole()))
                                        .sendTime(dto.getSendTimeStamp())
                                        .build())
                                .collect(Collectors.toList()))
                        .sessionConfigVO(SessionConfigVO.builder()
                                .model(chatRequest.getModel())
                                .build())
                        .openid(openid)
                        .build();
                sessionId = chatService.createSession(sessionEntity);
                //在响应头中返回信息
                response.setHeader("X-Session-Id", sessionId);
                //构建参数
                chatProcessAggregate = ChatProcessAggregate.builder()
                        .openid(openid)
                        .model(chatRequest.getModel())
                        .messages(chatRequest.getMessages().stream()
                                .map(questMessageDTO -> SessionMessageVO.builder()
                                        .role(ChatMessageRole.getByCode(questMessageDTO.getRole()))
                                        .content(questMessageDTO.getContent())
                                        .sendTime(questMessageDTO.getSendTimeStamp())
                                        .build())
                                .collect(Collectors.toList()))
                        .sessionId(sessionId)
                        .build();
            } else {
                //校验会话是否存在
                if (!chatService.validateSession(sessionId, openid)) {
                    try {
                        emitter.send(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    emitter.complete();
                    return emitter;
                }
                //构建参数
                chatProcessAggregate = ChatProcessAggregate.builder()
                        .openid(openid)
                        .model(chatRequest.getModel())
                        .messages(chatRequest.getMessages().stream()
                                .map(questMessageDTO -> SessionMessageVO.builder()
                                        .role(ChatMessageRole.getByCode(questMessageDTO.getRole()))
                                        .content(questMessageDTO.getContent())
                                        .sendTime(questMessageDTO.getSendTimeStamp())
                                        .build())
                                .collect(Collectors.toList()))
                        .sessionId(sessionId)
                        .build();

                //6.追加会话消息
                chatService.appendMessageToSession(ChatMessageEvent.builder()
                        .chatProcess(chatProcessAggregate)
                        .sessionMessageVO(chatProcessAggregate.getLastMessage())
                        .build());
            }


            //7.请求结果&返回
            return chatService.completions(emitter, chatProcessAggregate);

        } catch (Exception e) {
            log.error("流式应答，请求模型：{} 发生异常", chatRequest.getModel(), e);
            throw new ChatGLMException(e.getMessage());
        }

    }


    @RequestMapping(value = "query_session_headers", method = RequestMethod.GET)
    public Response<List<SessionHeaderDTO>> querySessionHeaders(@RequestHeader("Authorization") String token) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<List<SessionHeaderDTO>>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            //2.token解析
            String openid = authService.openid(token);

            //3.查询对话头信息
            List<SessionHeaderEntity> sessionHeaderEntityList = chatService.querySessionHeaders(openid);
            List<SessionHeaderDTO> sessionHeaderDTOList = new ArrayList<>(sessionHeaderEntityList.size());
            for (SessionHeaderEntity sessionHeaderEntity : sessionHeaderEntityList) {
                SessionHeaderDTO sessionHeaderDTO = SessionHeaderDTO.builder()
                        .sessionTitle(sessionHeaderEntity.getSessionTitle())
                        .updateTimeStamp(sessionHeaderEntity.getUpdateTimeStamp())
                        .sessionId(sessionHeaderEntity.getSessionId())
                        .sessionConfig(sessionHeaderEntity.getSessionConfigVO().getModel())
                        .build();
                sessionHeaderDTOList.add(sessionHeaderDTO);
            }


            return Response.<List<SessionHeaderDTO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(sessionHeaderDTOList)
                    .build();


        } catch (Exception e) {
            log.error("聊天标题查询失败", e);
            return Response.<List<SessionHeaderDTO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_session_messages", method = RequestMethod.GET)
    public Response<List<QuestMessageDTO>> querySessionMessages(@RequestHeader("Authorization") String token, @RequestParam String sessionId) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<List<QuestMessageDTO>>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            //2.token解析
            String openid = authService.openid(token);

            //3.查询聊天消息
            if (null != sessionId && !StringUtil.isBlank(sessionId)) {
                List<SessionMessageVO> sessionMessageVOList = chatService.querySessionMessages(openid, sessionId);
                List<QuestMessageDTO> sessionQuestMessageDTOList = new ArrayList<>(sessionMessageVOList.size());
                for (SessionMessageVO sessionMessageVO : sessionMessageVOList) {
                    QuestMessageDTO questMessageDTO = QuestMessageDTO.builder()
                            .content(sessionMessageVO.getContent())
                            .role(sessionMessageVO.getRole().getCode())
                            .sendTimeStamp(sessionMessageVO.getSendTime())
                            .build();
                    sessionQuestMessageDTOList.add(questMessageDTO);
                }

                return Response.<List<QuestMessageDTO>>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(sessionQuestMessageDTOList)
                        .build();
            }

            return Response.<List<QuestMessageDTO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(new ArrayList<>())
                    .build();

        } catch (Exception e) {
            log.error("聊天消息查询失败", e);
            return Response.<List<QuestMessageDTO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "delete_session", method = RequestMethod.DELETE)
    public Response deleteSession(@RequestHeader("Authorization") String token, @RequestParam String sessionId) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            //2.token解析
            String openid = authService.openid(token);

            //3.删除session
            chatService.deleteSession(openid, sessionId);


            return Response.builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();

        } catch (Exception e) {
            log.error("聊天删除失败", e);
            return Response.builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "change_session_title", method = RequestMethod.PUT)
    public Response changeSessionTitle(@RequestHeader("Authorization") String token, @RequestBody ChangeSessionTitleRequestDTO requestDTO) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            //2.token解析
            String openid = authService.openid(token);

            //3.修改标题
            chatService.changeSessionTitle(openid, requestDTO.getSessionId(), requestDTO.getNewTitle());

            return Response.builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("聊天标题修改失败", e);
            return Response.builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


}
