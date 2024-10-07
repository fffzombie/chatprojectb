package com.zombie.chatglm.data.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * ClassName: PrometheusConfiguration
 * Package: com.zombie.chatglm.data.config
 * Description: Prometheus 采集
 *
 * @Author ME
 * @Create 2024/10/6 15:26
 * @Version 1.0
 */
@EnableAspectJAutoProxy
@Configuration
public class PrometheusConfiguration {

    @Bean
    public CollectorRegistry collectorRegistry() {
        return new CollectorRegistry();
    }

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry(PrometheusConfig config, CollectorRegistry collectorRegistry) {
        return new PrometheusMeterRegistry(config, collectorRegistry, Clock.SYSTEM);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}
