package com.zombie.chatglm.data.domain.openai.service;

import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionEntity;
import com.zombie.chatglm.data.domain.openai.model.event.ChatMessageEvent;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionHeaderEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;


/***
 *
 * @Description 处理ai请求的接口
 * @return
 *
 */
public interface IChatService {
    ResponseBodyEmitter completions(ResponseBodyEmitter emitter, ChatProcessAggregate chatProcess);

    List<SessionHeaderEntity> querySessionHeaders(String openid);

    List<SessionMessageVO> querySessionMessages(String openid, String sessionId);

    String createSession(SessionEntity sessionEntity);

    boolean validateSession(String sessionId, String openid);

    void deleteSession(String openid, String sessionId);

    void changeSessionTitle(String openid, String sessionId, String newTitle);

    void appendMessageToSession(ChatMessageEvent messageEvent);
}
