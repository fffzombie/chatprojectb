package com.zombie.chatglm.data.domain.order.repository;

import com.zombie.chatglm.data.domain.order.model.aggregates.CreateOrderAggregate;
import com.zombie.chatglm.data.domain.order.model.entity.PayOrderEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ProductEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ShopCartEntity;
import com.zombie.chatglm.data.domain.order.model.entity.UnpaidOrderEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 订单仓储接口
 */
public interface IOrderRepository {


    UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity);

    ProductEntity queryProduct(Integer productId);

    void saveOrder(CreateOrderAggregate aggregate);

    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal payAmount, Date payTime);

    void deliverGoods(String orderId);
}
