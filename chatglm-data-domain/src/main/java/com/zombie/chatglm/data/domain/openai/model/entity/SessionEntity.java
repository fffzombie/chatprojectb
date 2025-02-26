package com.zombie.chatglm.data.domain.openai.model.entity;

import com.zombie.chatglm.data.domain.openai.model.valobj.SessionConfigVO;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {
    /**
     * 会话id
     */
    private String sessionId;
    /**
     * 对话标题
     */
    private String sessionTitle;

    /**
     * 聊天消息
     */
    private List<SessionMessageVO> messageList;

    /**
     * 对话配置
     */
    private SessionConfigVO sessionConfigVO;

    /**
     * 用户id
     */
    private String openid;

}
