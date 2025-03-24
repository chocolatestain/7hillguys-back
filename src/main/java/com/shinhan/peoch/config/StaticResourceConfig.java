package com.shinhan.peoch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /image/** URL로 요청하면, file system의 해당 경로에서 파일을 찾음.
        String imagesPath = "file:" + System.getProperty("user.dir")
                + "/src/main/resources/design/image/";
        registry.addResourceHandler("/image/**")
                .addResourceLocations(imagesPath);
    }
}