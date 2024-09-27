package com.zombie.chatglm.data.domain.openai.service.rule.factory;

import com.zombie.chatglm.data.domain.openai.annotation.LogicStrategy;
import com.zombie.chatglm.data.domain.openai.service.rule.ILogicFilter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 规则工厂
 */
@Service
public class DefaultLogicFactory {

    //保存所有的过滤接口实现类
    public Map<String, ILogicFilter> logicFilterMap = new ConcurrentHashMap<>();


    //spring自动从形参注入接口实现类，通过实现类上的自定义注解生成对应的key value存放到logicFilterMap
    public DefaultLogicFactory(List<ILogicFilter> logicFilters) {
        logicFilters.forEach(logic ->{
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(), LogicStrategy.class);
            if(strategy != null){
                logicFilterMap.put(strategy.logicMode().getCode(),logic);
            }
        });
    }

    public Map<String, ILogicFilter> openLogicFilter(){
        return logicFilterMap;
    }

    /**
     * 规则逻辑枚举
     */
    public enum LogicModel {

        ACCESS_LIMIT("ACCESS_LIMIT", "访问次数过滤"),
        SENSITIVE_WORD("SENSITIVE_WORD", "敏感词过滤"),
        ;

        private String code;
        private String info;

        LogicModel(String code, String info) {
            this.code = code;
            this.info = info;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

}
