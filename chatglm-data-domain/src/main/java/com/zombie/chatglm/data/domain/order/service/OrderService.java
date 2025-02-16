package com.zombie.chatglm.data.domain.order.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zombie.chatglm.data.domain.order.model.aggregates.CreateOrderAggregate;
import com.zombie.chatglm.data.domain.order.model.entity.OrderEntity;
import com.zombie.chatglm.data.domain.order.model.entity.PayOrderEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ProductEntity;
import com.zombie.chatglm.data.domain.order.model.valobj.OrderStatusVO;
import com.zombie.chatglm.data.domain.order.model.valobj.PayStatusVO;
import com.zombie.chatglm.data.domain.order.model.valobj.PayTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class OrderService extends AbstractOrderService{

    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;

    @Resource
    private AlipayClient alipayClient;


    @Override
    protected OrderEntity doSaveOrder(String openid, ProductEntity productEntity) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        orderEntity.setOrderTime(new Date());
        log.info("orderEntity.orderTime:{}",orderEntity.getOrderTime());
        orderEntity.setOrderStatus(OrderStatusVO.CREATE);
        orderEntity.setTotalAmount(productEntity.getPrice());
        orderEntity.setPayTypeVO(PayTypeVO.ALIPAY_SANDBOX);
        // 聚合信息
        CreateOrderAggregate aggregate = CreateOrderAggregate.builder()
                .openid(openid)
                .product(productEntity)
                .order(orderEntity)
                .build();
        // 保存订单；订单和支付，是2个操作。
        // 一个是数据库操作，一个是HTTP操作。所以不能一个事务处理，只能先保存订单再操作创建支付单，如果失败则需要任务补偿
        orderRepository.saveOrder(aggregate);
        return orderEntity;
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal){
        //支付宝沙箱支付
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId); // 我们自己生成的订单编号
        bizContent.put("total_amount", amountTotal.toString()); // 订单的总金额
        bizContent.put("subject", productName); // 支付的名称
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 固定配置
        //设置支付宝沙箱超时时间
        bizContent.put("timeout_express", "30m");
        request.setBizContent(bizContent.toString());
        String form = null;
        try{
            form = alipayClient.pageExecute(request).getBody();
        }catch (AlipayApiException e){
            log.info("阿里支付异常:{}",e.getErrMsg());
        }

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOpenid(openid);
        payOrderEntity.setOrderId(orderId);
        payOrderEntity.setPayUrl(form);
        payOrderEntity.setPayStatus(PayStatusVO.WAIT);

        //更新订单支付信息
        orderRepository.updateOrderPayInfo(payOrderEntity);
        return payOrderEntity;
    }


    @Override
    public boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal payAmount, Date payTime) {
        return orderRepository.changeOrderPaySuccess(orderId,transactionId,payAmount,payTime);
    }


    @Override
    public void deliverGoods(String orderId) {
        orderRepository.deliverGoods(orderId);
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderRepository.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderRepository.changeOrderClose(orderId);
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderRepository.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryReplenishmentOrder() {
        return orderRepository.queryReplenishmentOrder();
    }

    @Override
    public List<ProductEntity> queryProductList() {
        return orderRepository.queryProductList();
    }


}
