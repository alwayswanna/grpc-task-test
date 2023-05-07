package a.gleb.grpcclientapp.configuration;

import a.gleb.grpcclientapp.configuration.properties.GrpcClientConfigurationProperties;
import a.gleb.grpcclientapp.interceptor.LogGrpcInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GrpcClientConfigurationProperties.class)
public class GrpcClientConfig {

    @Bean
    LogGrpcInterceptor logGrpcInterceptor() {
        return new LogGrpcInterceptor();
    }

    @Bean
    public ExecutorService executor(GrpcClientConfigurationProperties properties) {
        var executorProperties = properties.getExecutorConfig();

        return new ThreadPoolExecutor(
                executorProperties.getCorePoolSize(),
                executorProperties.getMaxPoolSize(),
                executorProperties.getKeepAliveInMinutes(),
                MINUTES,
                new LinkedBlockingQueue<>()
        );
    }
}
