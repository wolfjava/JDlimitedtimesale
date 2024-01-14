package cn.wolfcode.web.config;

import cn.wolfcode.common.web.interceptor.FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by lanxw
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor(){
        return new FeignRequestInterceptor();
    }
}
