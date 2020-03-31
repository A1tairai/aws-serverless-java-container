package com.amazonaws.serverless.proxy.spring.echoapp;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.AwsProxyExceptionHandler;
import com.amazonaws.serverless.proxy.AwsProxySecurityContextWriter;
import com.amazonaws.serverless.proxy.InitializationWrapper;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletRequestReader;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletResponseWriter;
import com.amazonaws.serverless.proxy.internal.servlet.AwsServletRegistration;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringLambdaContainerHandler;
import com.amazonaws.serverless.proxy.spring.SpringProxyHandlerBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;

import java.util.EnumSet;


@Configuration
@ComponentScan("com.amazonaws.serverless.proxy.spring.echoapp")
@PropertySource("classpath:application.properties")
public class EchoSpringAppConfig {

    @Autowired
    private ConfigurableWebApplicationContext applicationContext;

    @Bean
    public SpringLambdaContainerHandler springLambdaContainerHandler() throws ContainerInitializationException {
        SpringLambdaContainerHandler handler = new SpringProxyHandlerBuilder()
                .defaultProxy()
                .initializationWrapper(new InitializationWrapper())
                .springApplicationContext(applicationContext)
                .buildAndInitialize();
        handler.onStartup(c -> {
            FilterRegistration.Dynamic registration = c.addFilter("UnauthenticatedFilter", UnauthenticatedFilter.class);
            // update the registration to map to a path
            registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/echo/*");
            // servlet name mappings are disabled and will throw an exception

            //handler.getApplicationInitializer().getDispatcherServlet().setThrowExceptionIfNoHandlerFound(true);
            ((DispatcherServlet)((AwsServletRegistration)c.getServletRegistration("dispatcherServlet")).getServlet()).setThrowExceptionIfNoHandlerFound(true);

        });
        return handler;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MockLambdaContext lambdaContext() {
        return new MockLambdaContext();
    }

    @Bean
    public javax.validation.Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
