package com.zombie.chatglm.data.infrastructure.repository;

import com.zombie.chatglm.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.UserAccountQuotaEntity;
import com.zombie.chatglm.data.domain.openai.model.entity.SessionHeaderEntity;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionConfigVO;
import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.domain.openai.model.valobj.UserAccountStatusVO;
import com.zombie.chatglm.data.domain.openai.repository.IOpenAiRepository;
import com.zombie.chatglm.data.infrastructure.dao.IUserAccountDao;
import com.zombie.chatglm.data.infrastructure.mongodb.ChatSessionRepositoryService;
import com.zombie.chatglm.data.infrastructure.po.mongodb.SessionsPO;
import com.zombie.chatglm.data.infrastructure.po.mysql.UserAccountPO;
import com.zombie.chatglm.data.infrastructure.redis.IRedisService;
import com.zombie.chatglm.data.types.enums.ChatMessageRole;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description OpenAi 仓储服务
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private IRedisService redisService;

    @Resource
    ChatSessionRepositoryService chatSessionRepositoryService;

    private static final String REDIS_USER_FREE_PREFIX = "user:freecount:";

    @Override
    public UserAccountQuotaEntity queryUserAccount(String openid) {

        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return null;
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

    @Override
    public Integer queryFreeCount(String openid) {
        String key = REDIS_USER_FREE_PREFIX + openid;
        return redisService.<Integer>getValue(key);
    }

    @Override
    public void setUserFreeCount(String openid, Integer count) {
        redisService.setIfAbsent(REDIS_USER_FREE_PREFIX + openid, count, 24 * 60 * 60 * 1000);
    }

    @Override
    public void subUserFreeCount(String openid) {
        Integer count = redisService.<Integer>getValue(REDIS_USER_FREE_PREFIX + openid);
        if (count > 0) {
//            redisService.setValue(REDIS_USER_FREE_PREFIX + openid,count - 1);
            redisService.decr(REDIS_USER_FREE_PREFIX + openid);
        }
    }

    @Override
    public void updateSessionMessage(SessionMessageVO sessionMessageVO, ChatProcessAggregate chatProcess) {
        chatSessionRepositoryService.updateSessionMessage(
                chatProcess.getSessionId(),
                chatProcess.getOpenid(),
                SessionsPO.SessionConfig.builder()
                        .model(chatProcess.getModel())
                        .build(),
                SessionsPO.SessionMessage.builder()
                        .sendTime(sessionMessageVO.getSendTime())
                        .role(sessionMessageVO.getRole().getCode())
                        .content(sessionMessageVO.getContent())
                        .build(),
                System.currentTimeMillis()
        );
    }

    @Override
    public List<SessionHeaderEntity> queryMessageHeaders(String openid) {
        List<SessionsPO> poList = chatSessionRepositoryService.findSessionHeaderByOpenId(openid);
        List<SessionHeaderEntity> voList = new ArrayList<>(poList.size());
        for (SessionsPO sessionsPO : poList) {
            SessionHeaderEntity sessionHeaderEntity = SessionHeaderEntity.builder()
                    .sessionId(sessionsPO.getSessionId())
                    .sessionTitle(sessionsPO.getSessionTitle())
                    .updateTimeStamp(sessionsPO.getUpdateTime())
                    .sessionConfigVO(SessionConfigVO.builder()
                            .model(sessionsPO.getSessionConfig().getModel())
                            .build())
                    .build();
            voList.add(sessionHeaderEntity);
        }
        return voList;
    }

    @Override
    public List<SessionMessageVO> querySessionMessages(String openid, String sessionId) {
        SessionsPO po = chatSessionRepositoryService.querySessionMessages(openid, sessionId);
        List<SessionMessageVO> sessionMessageVOList = new ArrayList<>(po.getMessageList().size());
        for (SessionsPO.SessionMessage sessionMessage : po.getMessageList()) {
            SessionMessageVO messageVO = SessionMessageVO.builder()
                    .content(sessionMessage.getContent())
                    .sendTime(sessionMessage.getSendTime())
                    .role(ChatMessageRole.getByCode(sessionMessage.getRole()))
                    .build();
            sessionMessageVOList.add(messageVO);
        }
        return sessionMessageVOList;
    }

    @Override
    public void insertSession(SessionEntity sessionEntity) {
        SessionsPO po = SessionsPO.builder()
                .sessionId(sessionEntity.getSessionId())
                .sessionTitle(sessionEntity.getSessionTitle())
                .sessionConfig(SessionsPO.SessionConfig.builder()
                        .model(sessionEntity.getSessionConfigVO().getModel())
                        .build())
                .messageList(sessionEntity.getMessageList().stream()
                        .map(vo -> SessionsPO.SessionMessage.builder()
                                .content(vo.getContent())
                                .role(vo.getRole().getCode())
                                .sendTime(vo.getSendTime())
                                .build())
                        .collect(Collectors.toList()))
                .openid(sessionEntity.getOpenid())
                .build();
        po.setCreateTime(System.currentTimeMillis());
        po.setUpdateTime(System.currentTimeMillis());
        chatSessionRepositoryService.insert(po);
    }

    @Override
    public boolean validateSession(String sessionId, String openid) {
        long count = chatSessionRepositoryService.countByOpenidAndSessionId(openid, sessionId);
        return count > 0;
    }

    @Override
    public void deleteSession(String openid, String sessionId) {
        chatSessionRepositoryService.deleteBySessionIdAndOpenid(sessionId, openid);
    }

    @Override
    public void changeSessionTitle(String openid, String sessionId, String newTitle) {
        chatSessionRepositoryService.updateSessionTitle(openid, sessionId, newTitle, System.currentTimeMillis());
    }

}
