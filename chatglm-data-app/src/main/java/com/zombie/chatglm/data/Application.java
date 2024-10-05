package com.zombie.chatglm.data;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/***
 *
 * @Description 启动类
 * @Date 15:23 2024/9/19
 *
 *
 */
@SpringBootApplication
@Configurable
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
