package faang.school.analytics.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import faang.school.analytics.config.UserContext;

@Configuration
public class FeignConfig {

    @Bean
    public FeignUserInterceptor feignUserInterceptor(UserContext userContext) {
        return new FeignUserInterceptor(userContext);
    }
}
