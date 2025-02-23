package com.zombie.chatglm.data.domain.account.repository;

import com.zombie.chatglm.data.domain.account.model.entity.OrderEntity;
import com.zombie.chatglm.data.domain.account.model.valobj.AccountQuotaVO;

import java.util.List;

/**
 * @description 账户仓储服务
 */
public interface IAccountRepository {

    AccountQuotaVO queryAccountQuota(String openId);

    List<OrderEntity> queryAccountOrderList(Integer pageNum, Integer pageSize, String openid);

    Integer queryAccountOrderCount(String openid);
}
