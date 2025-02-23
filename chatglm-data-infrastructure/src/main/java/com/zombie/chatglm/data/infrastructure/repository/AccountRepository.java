package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.account.model.entity.OrderEntity;
import com.zombie.chatglm.data.domain.account.model.valobj.*;
import com.zombie.chatglm.data.domain.account.repository.IAccountRepository;
import com.zombie.chatglm.data.infrastructure.dao.IOpenAIOrderDao;
import com.zombie.chatglm.data.infrastructure.dao.IUserAccountDao;
import com.zombie.chatglm.data.infrastructure.po.mysql.OpenAIOrderPO;
import com.zombie.chatglm.data.infrastructure.po.mysql.UserAccountPO;
import com.zombie.chatglm.data.infrastructure.redis.IRedisService;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AccountRepository implements IAccountRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private IOpenAIOrderDao openAIOrderDao;

    @Resource
    private IRedisService redisService;

    private static final String REDIS_USER_FREE_PREFIX = "user:freecount:";

    private static final Integer FREE_COUNT = 3;

    @Override
    public AccountQuotaVO queryAccountQuota(String openId) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openId);
        Integer freeQuota = redisService.getValue(REDIS_USER_FREE_PREFIX + openId);
        if (null == userAccountPO) {
            return AccountQuotaVO.builder()
                    .totalQuota(0)
                    .surplusQuota(0)
                    .freeQuota(0)
                    .build();
        }
        if (null == freeQuota) {
            return AccountQuotaVO.builder()
                    .totalQuota(userAccountPO.getTotalQuota())
                    .surplusQuota(userAccountPO.getSurplusQuota())
                    .freeQuota(FREE_COUNT)
                    .build();
        }
        return AccountQuotaVO.builder()
                .totalQuota(userAccountPO.getTotalQuota())
                .surplusQuota(userAccountPO.getSurplusQuota())
                .freeQuota(freeQuota)
                .build();
    }

    @Override
    public List<OrderEntity> queryAccountOrderList(Integer pageNum, Integer pageSize, String openid) {

        Integer offset = (pageNum - 1) * pageSize;

        List<OpenAIOrderPO> orderPOList = openAIOrderDao.queryAccountOrderList(offset, pageSize, openid);
        List<OrderEntity> orderEntityList = new ArrayList<>(orderPOList.size());

        for (OpenAIOrderPO openAIOrderPO : orderPOList) {
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setOrderId(openAIOrderPO.getOrderId());
            orderEntity.setOrderTime(openAIOrderPO.getOrderTime());
            orderEntity.setPayTime(openAIOrderPO.getPayTime());
            orderEntity.setOrderStatus(OrderStatusVO.get(openAIOrderPO.getOrderStatus()));
            orderEntity.setPayStatusVO(PayStatusVO.get(openAIOrderPO.getPayStatus()));
            orderEntity.setTotalAmount(openAIOrderPO.getTotalAmount());
            orderEntity.setPayTypeVO(PayTypeVO.get(openAIOrderPO.getPayType()));
            orderEntity.setProductVo(new ProductVo(openAIOrderPO.getProductName(), openAIOrderPO.getProductQuota()));

            orderEntityList.add(orderEntity);
        }
        return orderEntityList;
    }

    @Override
    public Integer queryAccountOrderCount(String openid) {
        return openAIOrderDao.queryAccountOrderCount(openid);
    }

}
