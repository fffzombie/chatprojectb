package com.zombie.chatglm.data.infrastructure.po.mongodb;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.repository.NoRepositoryBean;

@Data
@NoRepositoryBean
public abstract class BaseDocument {
    @Id
    private String id;
    @Field("create_time")
    private Long createTime;
    @Field("update_time")
    private Long updateTime;

}
