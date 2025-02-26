package com.zombie.chatglm.data.domain.openai.model.entity;

import com.zombie.chatglm.data.domain.openai.model.valobj.SessionConfigVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionHeaderEntity {
    /**
     * 对话ID
     */
    private String sessionId;
    /**
     * 对话标题
     */
    private String sessionTitle;
    /**
     * 最后更新时间戳
     */
    private Long updateTimeStamp;
    /**
     *  对话配置信息
     */
    private SessionConfigVO sessionConfigVO;
}
