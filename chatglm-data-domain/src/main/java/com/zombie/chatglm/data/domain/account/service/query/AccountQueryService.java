package com.zombie.chatglm.data.domain.account.service.query;

import com.zombie.chatglm.data.domain.account.model.entity.OrderEntity;
import com.zombie.chatglm.data.domain.account.model.valobj.AccountQuotaVO;
import com.zombie.chatglm.data.domain.account.repository.IAccountRepository;
import com.zombie.chatglm.data.domain.account.service.IAccountQueryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AccountQueryService implements IAccountQueryService {

    @Resource
    private IAccountRepository repository;

    @Override
    public AccountQuotaVO queryAccountQuota(String openId) {
        return repository.queryAccountQuota(openId);
    }

    @Override
    public List<OrderEntity> queryAccountOrderList(Integer pageNum, Integer pageSize, String openid) {
        return repository.queryAccountOrderList(pageNum,pageSize,openid);
    }

    @Override
    public Integer queryAccountOrderCount(String openid) {
        return repository.queryAccountOrderCount(openid);
    }

}
