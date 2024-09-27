package com.zombie.chatglm.data.domain.openai.model.entity;

import com.zombie.chatglm.data.domain.openai.model.valobj.LogicCheckTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 规则校验结果实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleLogicEntity<T> {
    //检验结果
    private LogicCheckTypeVO type;
    //提示信息
    private String info;
    private T data;
}
