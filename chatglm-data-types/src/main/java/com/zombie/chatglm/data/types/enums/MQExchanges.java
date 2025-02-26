package com.zombie.chatglm.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MQExchanges {
    CHAT_ROUTER("chat.exchange");

    private String routerName;
}
