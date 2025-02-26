package com.zombie.chatglm.data.infrastructure.mongodb;

import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.infrastructure.po.mongodb.SessionsPO;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepositoryService extends IMongoDBRepository<SessionsPO> {
    @Query(value = "{'openid': ?0}",fields = "{'session_title': 1,'session_id': 1,'update_time': 1,'session_config': 1}")
    List<SessionsPO> findSessionHeaderByOpenId(String openid);

    @Query(value = "{'openid': ?0,'session_id': ?1}",fields = "{'session_messages': 1,'session_config': 1}")
    SessionsPO querySessionMessages(String openid, String sessionId);

    Long countByOpenidAndSessionId(String openid, String sessionId);

    @Query(value = "{'sessionId': ?0,'openid': ?1}")
    @Update("{'$push': {'session_messages': ?3}, '$set':{'session_config': ?2,'update_time': ?4}}")
    void updateSessionMessage(String sessionId, String openid, SessionsPO.SessionConfig sessionConfig, SessionsPO.SessionMessage sessionMessage,long updateTime);

    void deleteBySessionIdAndOpenid(String sessionId,String openid);

    @Query(value = "{'openid': ?0,'session_id': ?1}")
    @Update("{'$set': {'session_title': ?2,'update_time': ?3}}")
    void updateSessionTitle(String openid, String sessionId, String newTitle,long updateTime);
}
