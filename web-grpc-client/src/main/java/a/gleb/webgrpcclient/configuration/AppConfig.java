package a.gleb.webgrpcclient.configuration;

import a.gleb.service.GreetingServiceGrpc;
import a.gleb.service.ImageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
public class AppConfig implements WebMvcConfigurer {

    private static final int port = 50051;

    @Bean
    public ManagedChannel channel(){
        return ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
    }

    @Bean
    public GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub(){
        return GreetingServiceGrpc.newBlockingStub(channel());
    }

    @Bean
    public ImageServiceGrpc.ImageServiceStub asyncStub(){
        return ImageServiceGrpc.newStub(channel());
    }

    @Bean ImageServiceGrpc.ImageServiceBlockingStub imageServiceBlockingStub(){
        return ImageServiceGrpc.newBlockingStub(channel());
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
