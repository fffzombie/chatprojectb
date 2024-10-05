package com.zombie.chatglm.data.trigger.job;

import com.zombie.chatglm.data.domain.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * ClassName: TimeoutCloseOrderJob
 * Package: com.zombie.chatglm.data.trigger.job
 * Description:
 *  超时订单关闭任务
 *
 * @Author ME
 * @Create 2024/10/2 22:23
 * @Version 1.0
 */
@Slf4j
@Component
public class TimeoutCloseOrderJob {

    @Resource
    private IOrderService orderService;

    //定时任务，每10分钟执行一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec(){
        List<String> orderIds = orderService.queryTimeoutCloseOrderList();
        if(orderIds.isEmpty()){
            log.info("定时任务，超时30分钟订单关闭，暂无超时未支付订单 orderIds is null");
            return;
        }
        for (String orderId : orderIds) {
            //支付宝传入了支付有效时限为30分钟，不需要手动关闭订单
            boolean status = orderService.changeOrderClose(orderId);
            log.info("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
        }

    }
}
