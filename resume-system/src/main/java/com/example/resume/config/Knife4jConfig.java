package com.example.resume.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI resumeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智能简历评估与岗位推荐系统 API")
                        .description("简历解析 · 能力评估 · 人岗双向推荐")
                        .version("v1.0.0")
                        .contact(new Contact().name("ResumeSystem")));
    }
}
