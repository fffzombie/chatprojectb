package com.zombie.chatglm.data.infrastructure.dao;

import com.zombie.chatglm.data.infrastructure.po.mysql.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserAccountDao {
    UserAccountPO queryUserAccount(String openid);

    int subAccountQuota(String openid);

    int addAccountQuota(UserAccountPO userAccountPOReq);

    void insert(UserAccountPO userAccountPOReq);
}
