package com.zombie.chatglm.data.domain.openai.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionConfigVO {
    /**
     * 对话模型配置
     */
    private String model;

}
