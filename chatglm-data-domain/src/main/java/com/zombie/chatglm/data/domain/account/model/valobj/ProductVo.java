package com.zombie.chatglm.data.domain.account.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 商品的简单信息
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVo {
    /**
     * 商品名称
     */
    private String productName;

    /**
     * 额度次数
     */
    private Integer quota;
}
