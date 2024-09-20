package com.zombie.chatglm.data.trigger.http.dto;

import com.zombie.chatglm.data.types.enums.ChatGLMModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGLMRequestDTO {
    /** 默认模型 */
    private String model = ChatGLMModel.CHATGLM_TURBO.getCode();

    /** 问题描述 */
    private List<MessageEntity> messages;
}
