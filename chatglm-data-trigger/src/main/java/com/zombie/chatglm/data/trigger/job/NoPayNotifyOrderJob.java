package com.zombie.chatglm.data.trigger.job;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.common.eventbus.EventBus;
import com.zombie.chatglm.data.domain.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ClassName: NoPayNotifyOrderJob
 * Package: com.zombie.chatglm.data.trigger.job
 * Description:检测未收到或未正确处理的支付回调的订单的支付状态的补偿任务
 *
 * @Author ME
 * @Create 2024/10/3 16:20
 * @Version 1.0
 */
//TODO:未验证
@Slf4j
@Component
public class NoPayNotifyOrderJob {

    @Resource
    private IOrderService orderService;

    @Resource
    private AlipayClient alipayClient;
    @Resource
    private EventBus eventBus;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

    @Scheduled(cron = "0 0/3 * * * ?")
    public void exec(){
        try{
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            if(orderIds.isEmpty()){
                log.info("定时任务，订单支付状态更新，暂无未更新订单 orderId is null");
                return;
            }
            for (String orderId : orderIds) {
                //查询支付宝数据库的订单结果
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                model.setOutTradeNo(orderId);
                request.setBizModel(model);
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                //交易单号数据
                String tradeStatus = response.getTradeStatus();
                log.info("支付回调失败补偿任务查询数据:orderId:{},tradeStatus:{}",orderId,tradeStatus);
                //确认支付宝端交易状态为成功后，更新订单
                if(tradeStatus.equals("TRADE_SUCCESS")){
                    String transactionId = response.getTradeNo();
                    String payAmount = response.getBuyerPayAmount();
                    String payTime = dateFormat.format(response.getSendPayDate());

                    //更新订单
                    boolean success = orderService.changeOrderPaySuccess(orderId, transactionId, new BigDecimal(payAmount), dateFormat.parse(payTime));
                    if(success){
                        //发布消息
                        eventBus.post(orderId);
                    }
                }

            }
        }catch (Exception e){
            log.error("定时任务，订单支付状态更新失败", e);
        }
    }

}
