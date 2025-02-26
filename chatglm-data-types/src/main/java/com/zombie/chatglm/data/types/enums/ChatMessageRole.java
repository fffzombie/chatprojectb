package com.zombie.chatglm.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatMessageRole {
    USER("user"),
    AI("system");

    private String code;

    public static ChatMessageRole getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (ChatMessageRole role : values()) {
            if (role.getCode().equalsIgnoreCase(code)) {  // 使用忽略大小写比较
                return role;
            }
        }
        throw new IllegalArgumentException("未知的消息角色类型: " + code);
    }
}
