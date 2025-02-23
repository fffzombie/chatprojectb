package com.zombie.chatglm.data.infrastructure.dao;

import com.zombie.chatglm.data.infrastructure.po.mysql.OpenAIProductPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOpenAIProductDao {

    OpenAIProductPO queryProductByProductId(Integer productId);


    List<OpenAIProductPO> queryProductList();
}
