package com.zombie.chatglm.data.domain.order.service;

import com.alipay.api.AlipayApiException;
import com.zombie.chatglm.data.domain.order.model.entity.*;
import com.zombie.chatglm.data.domain.order.model.valobj.PayStatusVO;
import com.zombie.chatglm.data.domain.order.repository.IOrderRepository;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractOrderService implements IOrderService{

    @Resource
    protected IOrderRepository orderRepository;


    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity){
        // 0. 基础信息
        String openid = shopCartEntity.getOpenid();
        Integer productId = shopCartEntity.getProductId();

        //1.查询有效的未支付订单，如果存在直接返回支付宝支付
        UnpaidOrderEntity unpaidOrderEntity = orderRepository.queryUnpaidOrder(shopCartEntity);
        if(null != unpaidOrderEntity && PayStatusVO.WAIT.equals(unpaidOrderEntity.getPayStatus()) && null != unpaidOrderEntity.getPayUrl()){
            log.info("创建订单-存在，已生成支付宝支付，返回 openid: {} orderId: {} payUrl: {}", openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getPayUrl());
            return PayOrderEntity.builder()
                    .openid(openid)
                    .orderId(unpaidOrderEntity.getOrderId())
                    .payUrl(unpaidOrderEntity.getPayUrl())
                    .payStatus(unpaidOrderEntity.getPayStatus())
                    .build();
        } else if (null != unpaidOrderEntity && null == unpaidOrderEntity.getPayUrl()) {
            log.info("创建订单-存在，未生成支付宝支付链接，返回 openid: {} orderId: {}", openid, unpaidOrderEntity.getOrderId());
            PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getTotalAmount());
            log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, payOrderEntity.getOrderId(), payOrderEntity.getPayUrl());
            return payOrderEntity;
        }

        //2.查询商品
        ProductEntity productEntity = orderRepository.queryProduct(productId);
        if(!productEntity.isAvailable()){
            throw new ChatGLMException(Constants.ResponseCode.ORDER_PRODUCT_ERR.getCode(),Constants.ResponseCode.ORDER_PRODUCT_ERR.getInfo());
        }

        //3.保存订单
        OrderEntity orderEntity = this.doSaveOrder(openid, productEntity);

        // 4. 创建支付
        PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, orderEntity.getOrderId(), productEntity.getProductName(), orderEntity.getTotalAmount());
        log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, orderEntity.getOrderId(), payOrderEntity.getPayUrl());


        return payOrderEntity;
    }

    protected abstract OrderEntity doSaveOrder(String openid, ProductEntity productEntity);

    protected abstract PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal);
}
