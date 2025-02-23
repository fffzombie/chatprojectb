package com.zombie.chatglm.data.trigger.http.dto;

import com.zombie.chatglm.data.domain.account.model.valobj.ProductVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountOrderResponseDTO {
    /**
     * 订单编号
     */
    private String orderId;
    /**
     * 下单时间
     */
    private Date orderTime;
    /**
     * 支付时间
     */
    private Date payTime;
    /**
     * 订单状态
     */
    private String orderStatus;
    /**
     * 支付状态
     */
    private String payStatus;
    /**
     * 订单金额
     */
    private BigDecimal totalAmount;
    /**
     * 支付类型
     */
    private String payType;
    /**
     * 商品简单信息
     */
    private ProductVo productVo;
}
