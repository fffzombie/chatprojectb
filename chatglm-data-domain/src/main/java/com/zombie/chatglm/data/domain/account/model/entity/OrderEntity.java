package com.zombie.chatglm.data.domain.account.model.entity;

import com.zombie.chatglm.data.domain.account.model.valobj.OrderStatusVO;
import com.zombie.chatglm.data.domain.account.model.valobj.PayStatusVO;
import com.zombie.chatglm.data.domain.account.model.valobj.PayTypeVO;
import com.zombie.chatglm.data.domain.account.model.valobj.ProductVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 订单实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {
    /** 订单编号 */
    private String orderId;
    /** 下单时间 */
    private Date orderTime;
    /** 支付时间 */
    private Date payTime;
    /** 订单状态；0-创建完成、1-等待发货、2-发货完成、3-系统关单 */
    private OrderStatusVO orderStatus;
    /** 支付状态 */
    private PayStatusVO payStatusVO;
    /** 订单金额 */
    private BigDecimal totalAmount;
    /** 支付类型 */
    private PayTypeVO payTypeVO;
    /** 商品简单信息 */
    private ProductVo productVo;
}
