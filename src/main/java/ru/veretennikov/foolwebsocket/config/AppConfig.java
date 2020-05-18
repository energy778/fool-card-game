package ru.veretennikov.foolwebsocket.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class AppConfig {

//    @Bean
//    public static PropertySourcesPlaceholderConfigurer placeholderConfigInDev(){
//        return new PropertySourcesPlaceholderConfigurer();
//    }

//    дублируют настройки в application.yml и переопределяют их
    @Bean
    public static MessageSource messageSource(){
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages");
        ms.setDefaultEncoding("windows-1251");
//        ms.setDefaultEncoding("UTF-8");
        return ms;
    }

}
