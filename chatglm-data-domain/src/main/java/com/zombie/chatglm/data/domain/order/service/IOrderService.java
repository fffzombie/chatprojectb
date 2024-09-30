package com.zombie.chatglm.data.domain.order.service;


import com.alipay.api.AlipayApiException;
import com.zombie.chatglm.data.domain.order.model.entity.PayOrderEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ShopCartEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 订单服务
 * 1. 用户下单 createOrder
 */
public interface IOrderService {

    /**
     * 用户下单，通过购物车信息，返回下单后的支付单
     *
     * @param shopCartEntity 简单购物车
     * @return 支付单实体对象
     */
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws AlipayApiException;

    /**
     * 变更；订单支付成功
     */
    boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal payAmount, Date payTime);

    /**
     * 订单商品发货
     *
     * @param orderId 订单ID
     */
    void deliverGoods(String orderId);
}