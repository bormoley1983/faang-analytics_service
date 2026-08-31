package faang.school.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "project-service", url = "http://${project-service.host}:${project-service.port}")
public interface ProjectServiceClient {

}
