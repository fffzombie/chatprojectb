package com.zombie.chatglm.data.domain.openai.model.valobj;

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
public class SessionMessageVO implements Serializable {
    /**
     * 聊天内容
     */
    private String content;
    /**
     *  消息发送者
     */
    private ChatMessageRole role;

    /**
     * 信息发送时间
     */
    private Long sendTime;

}
