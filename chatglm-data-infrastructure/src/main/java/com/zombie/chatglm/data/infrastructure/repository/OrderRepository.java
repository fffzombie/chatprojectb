package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.openai.model.valobj.UserAccountStatusVO;
import com.zombie.chatglm.data.domain.order.model.aggregates.CreateOrderAggregate;
import com.zombie.chatglm.data.domain.order.model.entity.*;
import com.zombie.chatglm.data.domain.order.model.valobj.PayStatusVO;
import com.zombie.chatglm.data.domain.order.repository.IOrderRepository;
import com.zombie.chatglm.data.infrastructure.dao.IOpenAIOrderDao;
import com.zombie.chatglm.data.infrastructure.dao.IOpenAIProductDao;
import com.zombie.chatglm.data.infrastructure.dao.IUserAccountDao;
import com.zombie.chatglm.data.infrastructure.po.mysql.OpenAIOrderPO;
import com.zombie.chatglm.data.infrastructure.po.mysql.OpenAIProductPO;
import com.zombie.chatglm.data.infrastructure.po.mysql.UserAccountPO;
import com.zombie.chatglm.data.types.enums.OpenAIProductEnableModel;
import com.zombie.chatglm.data.types.enums.OpenAIUserEnableModelTypes;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IOpenAIOrderDao openAIOrderDao;

    @Resource
    private IOpenAIProductDao openAIProductDao;

    @Resource
    private IUserAccountDao userAccountDao;

    @Override
    public UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity) {
        OpenAIOrderPO openAIOrderPOQue = new OpenAIOrderPO();
        openAIOrderPOQue.setOpenid(shopCartEntity.getOpenid());
        openAIOrderPOQue.setProductId(shopCartEntity.getProductId());
        OpenAIOrderPO openAIOrderPORes = openAIOrderDao.queryUnpaidOrder(openAIOrderPOQue);
        if(null == openAIOrderPORes) return null;

        return UnpaidOrderEntity.builder()
                .openid(shopCartEntity.getOpenid())
                .orderId(openAIOrderPORes.getOrderId())
                .productName(openAIOrderPORes.getProductName())
                .totalAmount(openAIOrderPORes.getTotalAmount())
                .payUrl(openAIOrderPORes.getPayUrl())
                .payStatus(PayStatusVO.get(openAIOrderPORes.getPayStatus()))
                .build();
    }

    @Override
    public ProductEntity queryProduct(Integer productId) {
        OpenAIProductPO openAIProductPO = openAIProductDao.queryProductByProductId(productId);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductId(openAIProductPO.getProductId());
        productEntity.setProductName(openAIProductPO.getProductName());
        productEntity.setProductDesc(openAIProductPO.getProductDesc());
        productEntity.setQuota(openAIProductPO.getQuota());
        productEntity.setPrice(openAIProductPO.getPrice());
        productEntity.setEnable(OpenAIProductEnableModel.get(openAIProductPO.getIsEnabled()));
        return productEntity;
    }

    @Override
    public void saveOrder(CreateOrderAggregate aggregate) {
        String openid = aggregate.getOpenid();
        OrderEntity order = aggregate.getOrder();
        ProductEntity product = aggregate.getProduct();
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setProductId(product.getProductId());
        openAIOrderPO.setProductName(product.getProductName());
        openAIOrderPO.setProductQuota(product.getQuota());
        openAIOrderPO.setOpenid(openid);
        openAIOrderPO.setOrderId(order.getOrderId());
        openAIOrderPO.setOrderTime(order.getOrderTime());
        openAIOrderPO.setOrderStatus(order.getOrderStatus().getCode());
        openAIOrderPO.setTotalAmount(order.getTotalAmount());
        openAIOrderPO.setPayType(order.getPayTypeVO().getCode());
        openAIOrderPO.setPayStatus(PayStatusVO.WAIT.getCode());
        openAIOrderDao.insert(openAIOrderPO);
    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOpenid(payOrderEntity.getOpenid());
        openAIOrderPO.setOrderId(payOrderEntity.getOrderId());
        openAIOrderPO.setPayUrl(payOrderEntity.getPayUrl());
        openAIOrderPO.setPayStatus(payOrderEntity.getPayStatus().getCode());
        openAIOrderDao.updateOrderPayInfo(openAIOrderPO);
    }

    @Override
    public boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal payAmount, Date payTime) {
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOrderId(orderId);
        openAIOrderPO.setTransactionId(transactionId);
        openAIOrderPO.setPayAmount(payAmount);
        openAIOrderPO.setPayTime(payTime);
        int count = openAIOrderDao.changeOrderPaySuccess(openAIOrderPO);
        return count == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 350, propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void deliverGoods(String orderId) {
        OpenAIOrderPO openAIOrderPO = openAIOrderDao.queryOrder(orderId);

        //1.变更发货状态
        int updateOrderStatusDeliverGoodsCount = openAIOrderDao.updateOrderStatusDeliverGoods(orderId);
        if (1 != updateOrderStatusDeliverGoodsCount) throw new RuntimeException("updateOrderStatusDeliverGoodsCount update count is not equal 1");

        //2.账户额度变更
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openAIOrderPO.getOpenid());
        UserAccountPO userAccountPOReq = new UserAccountPO();
        userAccountPOReq.setOpenid(openAIOrderPO.getOpenid());
        userAccountPOReq.setTotalQuota(openAIOrderPO.getProductQuota());
        userAccountPOReq.setSurplusQuota(openAIOrderPO.getProductQuota());
        if(null != userAccountPO){
            int addAccountQuotaCount = userAccountDao.addAccountQuota(userAccountPOReq);
            if (1 != addAccountQuotaCount) throw new RuntimeException("addAccountQuotaCount update count is not equal 1");
        }else {
            userAccountPOReq.setStatus(UserAccountStatusVO.AVAILABLE.getCode());
            userAccountPOReq.setModelTypes(OpenAIUserEnableModelTypes.ALL.getCode());
            userAccountDao.insert(userAccountPOReq);
        }
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return openAIOrderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return openAIOrderDao.changeOrderClose(orderId);
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return openAIOrderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryReplenishmentOrder() {
        return openAIOrderDao.queryReplenishmentOrder();
    }

    @Override
    public List<ProductEntity> queryProductList() {
        List<OpenAIProductPO> openAIProductPOList = openAIProductDao.queryProductList();
        List<ProductEntity> productEntityList = new ArrayList<>(openAIProductPOList.size());
        for (OpenAIProductPO openAIProductPO : openAIProductPOList) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId(openAIProductPO.getProductId());
            productEntity.setProductName(openAIProductPO.getProductName());
            productEntity.setProductDesc(openAIProductPO.getProductDesc());
            productEntity.setQuota(openAIProductPO.getQuota());
            productEntity.setPrice(openAIProductPO.getPrice());
            productEntityList.add(productEntity);
        }
        return productEntityList;
    }

}
