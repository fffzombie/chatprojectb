package com.zombie.chatglm.data.infrastructure.mongodb;

import com.zombie.chatglm.data.infrastructure.po.mongodb.BaseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IMongoDBRepository<T extends BaseDocument> extends MongoRepository<T, String> {


}
