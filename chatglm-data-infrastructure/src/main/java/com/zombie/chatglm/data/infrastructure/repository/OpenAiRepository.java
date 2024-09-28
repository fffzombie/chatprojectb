package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.UserAccountStatusVO;
import com.zombie.chatglm.data.domain.openai.repository.IOpenAiRepository;
import com.zombie.chatglm.data.infrastructure.dao.IUserAccountDao;
import com.zombie.chatglm.data.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @description OpenAi 仓储服务
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Override
    public UserAccountQuotaEntity queryUserAccount(String openid) {

        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if(null == userAccountPO) return null;
        UserAccountQuotaEntity userAccountQuotaEntity = new UserAccountQuotaEntity();
        userAccountQuotaEntity.setOpenid(userAccountPO.getOpenid());
        userAccountQuotaEntity.setUserAccountStatusVO(UserAccountStatusVO.get(userAccountPO.getStatus()));
        userAccountQuotaEntity.setSurplusQuota(userAccountPO.getSurplusQuota());
        userAccountQuotaEntity.setTotalQuota(userAccountPO.getTotalQuota());
        userAccountQuotaEntity.genModelTypes(userAccountPO.getModelTypes());

        return userAccountQuotaEntity;
    }

    @Override
    public int subAccountQuota(String openid) {

        return userAccountDao.subAccountQuota(openid);


    }
}
