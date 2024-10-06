package com.zombie.chatglm.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * ClassName: OpenAiChannel
 * Package: com.zombie.chatglm.data.types.exception
 * Description:OpenAi渠道
 *
 * @Author ME
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum OpenAiChannel {
    ChatGLM("ChatGLM"),
    ChatGPT("ChatGPT"),

    ;
    private final String code;

    public static OpenAiChannel getChannel(String model) {
        if (model.toLowerCase().contains("gpt")) return OpenAiChannel.ChatGPT;
        if (model.toLowerCase().contains("glm")) return OpenAiChannel.ChatGLM;
        return null;
    }
}
