package com.zombie.chatglm.data.types.exception;

public class ChatGLMException extends RuntimeException{
    /**
     * 异常码
     */
    private String code;

    /**
     * 异常信息
     */
    private String message;

    public ChatGLMException(String code) {
        this.code = code;
    }
    public ChatGLMException(String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public ChatGLMException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ChatGLMException(String code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        super.initCause(cause);
    }
}
