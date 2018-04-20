package com.bence.projector.server.mailsending;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.io.IOException;

@Configuration
public class FreemarkerConfiguration extends WebMvcConfigurerAdapter {
    public static final String NEW_SONG = "newSongPage";
    public static final String NEW_SUGGESTION = "newSuggestionPage";
    public static final String NEW_SONG_LINK = "newSongLinkPage";
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/resources/", "classpath:/static/",
            "classpath:/webapp/"};

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

    @Bean
    public FreeMarkerViewResolver freemarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        return resolver;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerConfig() {
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();

        try {
            freeMarkerConfigurer.setTemplateLoaderPath(findParent(NEW_SONG + ".ftl"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return freeMarkerConfigurer;
    }

    public String findParent(final String freemarkerName) throws IOException {
        Resource resource = new ClassPathResource(freemarkerName);
        return resource.getFile().getParent();
    }
}
