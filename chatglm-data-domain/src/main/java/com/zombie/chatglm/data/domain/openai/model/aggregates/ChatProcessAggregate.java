package com.zombie.chatglm.data.domain.openai.model.aggregates;

import com.zombie.chatglm.data.domain.openai.model.entity.MessageEntity;
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
public class ChatProcessAggregate {
    /** 验证信息 */
    private String token;
    /** 默认模型 */
    private String model = ChatGLMModel.CHATGLM_TURBO.getCode();
    /** 问题描述 */
    private List<MessageEntity> messages;
}
