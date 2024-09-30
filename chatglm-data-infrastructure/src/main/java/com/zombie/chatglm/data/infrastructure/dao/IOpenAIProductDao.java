package com.zombie.chatglm.data.infrastructure.dao;

import com.zombie.chatglm.data.infrastructure.po.OpenAIProductPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IOpenAIProductDao {

    OpenAIProductPO queryProductByProductId(Integer productId);
}
