package com.zombie.chatglm.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: ChatGPTModel
 * Package: com.zombie.chatglm.data.types.enums
 * Description:ChatGPT模型对象
 *
 * @Author ME
 * @Create 2024/10/5 18:19
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ChatGPTModel {

    /** gpt-3.5-turbo */
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    /** GPT4.0 */
    GPT_4("gpt-4"),
    GPT_4o("gpt-4o"),
    /** GPT4.0 超长上下文 */
    GPT_4_32K("gpt-4-32k"),
    ;
    private String code;

}
