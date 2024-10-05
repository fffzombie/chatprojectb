package com.zombie.chatglm.data.domain.order.service;


import com.alipay.api.AlipayApiException;
import com.zombie.chatglm.data.domain.order.model.entity.PayOrderEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ProductEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ShopCartEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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

    /**
     * 查询超时30分钟，未支付订单
     */
    List<String> queryTimeoutCloseOrderList();

    /**
     * 超过30分钟，修改订单状态关闭订单
     * @param orderId 订单id
     * @return
     */
    boolean changeOrderClose(String orderId);

    /**
     * 查询有效期内，未接收到支付回调的订单
     */
    List<String> queryNoPayNotifyOrder();


    /**
     *  查询已支付未发货的订单补货id
     * @return
     */
    List<String> queryReplenishmentOrder();

    List<ProductEntity> queryProductList();
}