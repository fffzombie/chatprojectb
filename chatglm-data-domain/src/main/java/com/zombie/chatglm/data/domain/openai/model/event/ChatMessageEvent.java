package com.zombie.chatglm.data.domain.openai.model.event;

import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.types.enums.ChatMessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent implements Serializable {
    /**
     * 新消息
     */
    private SessionMessageVO sessionMessageVO;

    /**
     * 对话聚合信息
     */
    private ChatProcessAggregate chatProcess;
}
