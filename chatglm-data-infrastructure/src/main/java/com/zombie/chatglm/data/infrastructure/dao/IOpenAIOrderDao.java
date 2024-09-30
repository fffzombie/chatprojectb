package com.zombie.chatglm.data.infrastructure.dao;

import com.zombie.chatglm.data.infrastructure.po.OpenAIOrderPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IOpenAIOrderDao {
    OpenAIOrderPO queryUnpaidOrder(OpenAIOrderPO openAIOrderPOQue);

    void insert(OpenAIOrderPO openAIOrderPO);

    void updateOrderPayInfo(OpenAIOrderPO openAIOrderPO);

    int changeOrderPaySuccess(OpenAIOrderPO openAIOrderPO);

    OpenAIOrderPO queryOrder(String orderId);

    int updateOrderStatusDeliverGoods(String orderId);
}
