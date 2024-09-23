package com.zombie.chatglm.data.domain.weixin.service.message;

import cn.bugstack.chatglm.model.EventType;
import com.google.common.cache.Cache;
import com.zombie.chatglm.data.domain.weixin.model.entity.MessageTextEntity;
import com.zombie.chatglm.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import com.zombie.chatglm.data.domain.weixin.model.valobj.MsgTypeVO;
import com.zombie.chatglm.data.domain.weixin.service.IWeiXinBehaviorService;
import com.zombie.chatglm.data.types.exception.ChatGLMException;
import com.zombie.chatglm.data.types.sdk.weixin.XmlUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WeiXinBehaviorService implements IWeiXinBehaviorService {

    @Value("${wx.config.originalid}")
    private String originalId;


    @Resource
    private Cache<String,String> codeCache;

    @Override
    public String acceptUserBehavior(UserBehaviorMessageEntity entity) {
        //消息类型为事件，则忽略
        if(MsgTypeVO.EVENT.getCode().equals(entity.getMsgType())){
            return "";
        }

        // Text 文本类型
        if(MsgTypeVO.TEXT.getCode().equals(entity.getMsgType())){
            //缓存验证码
            String isExistCode = codeCache.getIfPresent(entity.getOpenId());

            // 判断验证码 - 不考虑验证码重复问题
            if(StringUtils.isBlank(isExistCode)){
                //创建验证码
                String code = RandomStringUtils.randomNumeric(4);
                codeCache.put(code, entity.getOpenId());
                codeCache.put(entity.getOpenId(), code);
                isExistCode = code;
            }

            //反馈信息[文本]
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(entity.getOpenId());
            res.setFromUserName(originalId);
            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
            res.setMsgType("text");
            res.setContent(String.format("您的验证码为：%s 有效期%d分钟！", isExistCode, 3));
            return XmlUtil.beanToXml(res);
        }


        throw new ChatGLMException(entity.getMsgType() + " 未被处理的行为类型 Err！");
    }
}
