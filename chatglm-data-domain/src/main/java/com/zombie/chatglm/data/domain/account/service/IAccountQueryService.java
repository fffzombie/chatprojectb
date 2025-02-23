package com.zombie.chatglm.data.domain.account.service;

import com.zombie.chatglm.data.domain.account.model.entity.OrderEntity;
import com.zombie.chatglm.data.domain.account.model.valobj.AccountQuotaVO;

import java.util.List;

/**
 * @description 账户查询服务
 */
public interface IAccountQueryService {

    AccountQuotaVO queryAccountQuota(String openId);

    List<OrderEntity> queryAccountOrderList(Integer pageNum, Integer pageSize, String openid);

    Integer queryAccountOrderCount(String openid);
}
