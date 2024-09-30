package com.zombie.chatglm.data.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description 支付类型
 */
@Getter
@AllArgsConstructor
public enum PayTypeVO {

    ALIPAY_SANDBOX(0, "支付宝沙箱支付"),
            ;

    private final Integer code;
    private final String desc;

    public static PayTypeVO get(Integer code){
        switch (code){
            case 0:
                return PayTypeVO.ALIPAY_SANDBOX;
            default:
                return PayTypeVO.ALIPAY_SANDBOX;
        }
    }

}
