package com.zombie.chatglm.data.trigger.http.dto;

import com.zombie.chatglm.data.types.enums.ChatGLMModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAIRequestDTO {
    /**
     * 默认模型
     */
    private String model = ChatGLMModel.GLM_4_FLASH.getCode();

    /**
     * 问题描述
     */
    private List<QuestMessageDTO> messages;

    /**
     * 会话ID
     */
    @Nullable
    private String sessionId;
}
