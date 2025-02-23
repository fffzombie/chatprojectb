package com.zombie.chatglm.data.domain.account.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description 支付类型
 */
@Getter
@AllArgsConstructor
public enum PayTypeVO {

    ALIPAY_NATIVE(0,"支付宝支付"),
            ;

    private final Integer code;
    private final String desc;

    public static PayTypeVO get(Integer code){
        switch (code){
            case 0:
                return PayTypeVO.ALIPAY_NATIVE;
            default:
                return PayTypeVO.ALIPAY_NATIVE;
        }
    }

}
