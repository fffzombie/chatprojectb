package com.zombie.chatglm.data.domain.openai.model.aggregates;

import com.zombie.chatglm.data.domain.openai.model.valobj.SessionMessageVO;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.enums.ChatGLMModel;
import com.zombie.chatglm.data.types.enums.OpenAiChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatProcessAggregate implements Serializable {
    /** 用户id */
    private String openid;
    /** 默认模型 */
    private String model = ChatGLMModel.CHATGLM_TURBO.getCode();
    /** 问题描述 */
    private List<SessionMessageVO> messages;
    /** 会话id */
    private String sessionId;

    public boolean isWhiteList(String whiteListStr){
        String[] whiteList = whiteListStr.split(Constants.SPLIT);
        for (String whiteOpenId : whiteList) {
            if(whiteOpenId.equals(openid)) return true;
        }
        return false;
    }

    public OpenAiChannel getChannel(){return OpenAiChannel.getChannel(this.model);}

    public synchronized SessionMessageVO getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }
}
