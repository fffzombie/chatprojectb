package com.zombie.chatglm.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpenAIUserEnableModelTypes {
    ALL("gpt-4,gpt-4o,glm-4-flash"),
    GPT("gpt-4,gpt-4o"),
    GLM("glm-4-flash")
    ;
    private final String code;

}
