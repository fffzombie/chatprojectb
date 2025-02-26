package com.zombie.chatglm.data.infrastructure.po.mongodb;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "sessions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionsPO extends BaseDocument {
    /**
     * 会话id
     */
    @Field("session_id")
    private String sessionId;
    /**
     * 对话信息
     */
    @Field("session_title")
    private String sessionTitle;


    /**
     * 对话配置
     */
    @Field("session_config")
    private SessionConfig sessionConfig;


    /**
     * 聊天消息
     */
    @Field("session_messages")
    private List<SessionMessage> messageList;

    /**
     * 用户id
     */
    @Field("openid")
    private String openid;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SessionConfig {
        /**
         * 对话模型配置
         */
        private String model;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SessionMessage {
        /**
         * 聊天内容
         */
        private String content;
        /**
         * 消息发送者
         */
        private String role;

        /**
         * 信息发送时间
         */
        @Field("send_time")
        private Long sendTime;
    }

}
