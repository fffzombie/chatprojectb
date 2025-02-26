package com.zombie.chatglm.data.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionHeaderDTO {
    /**
     * 对话ID
     */
    private String sessionId;
    /**
     * 最后修改时间
     */
    private Long updateTimeStamp;
    /**
     * 聊天标题
     */
    private String sessionTitle;

    /**
     * 聊天设置
     */
    private String sessionConfig;
}
