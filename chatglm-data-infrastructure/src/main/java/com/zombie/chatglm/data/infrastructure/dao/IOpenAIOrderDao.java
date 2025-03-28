package com.zombie.chatglm.data.infrastructure.dao;

import com.zombie.chatglm.data.infrastructure.po.mysql.OpenAIOrderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOpenAIOrderDao {
    OpenAIOrderPO queryUnpaidOrder(OpenAIOrderPO openAIOrderPOQue);

    void insert(OpenAIOrderPO openAIOrderPO);

    void updateOrderPayInfo(OpenAIOrderPO openAIOrderPO);

    int changeOrderPaySuccess(OpenAIOrderPO openAIOrderPO);

    OpenAIOrderPO queryOrder(String orderId);

    int updateOrderStatusDeliverGoods(String orderId);

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryReplenishmentOrder();

    List<OpenAIOrderPO> queryAccountOrderList(Integer offset, Integer pageSize, String openid);

    Integer queryAccountOrderCount(String openid);
}
