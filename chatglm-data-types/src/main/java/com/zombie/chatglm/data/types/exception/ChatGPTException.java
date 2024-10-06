package com.zombie.chatglm.data.types.exception;

/**
 * ClassName: ChatGPTException
 * Package: com.zombie.chatglm.data.types.exception
 * Description:ChatGPT异常类
 *
 * @Author ME
 * @Create 2024/10/5 18:13
 * @Version 1.0
 */
public class ChatGPTException extends RuntimeException{
    /**
     * 异常码
     */
    private String code;
    /**
     * 异常信息
     */
    private String message;

    public ChatGPTException(String code) {
        this.code = code;
    }

    public ChatGPTException(String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public ChatGPTException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ChatGPTException(String code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        super.initCause(cause);
    }
}
