package a.gleb.grpcserverapp.configuration;

import a.gleb.grpcserverapp.configuration.properties.GrpcServerConfigurationProperties;
import a.gleb.grpcserverapp.interceptor.LogGrpcInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GrpcServerConfigurationProperties.class)
public class GrpcServerConfig {

    @GrpcGlobalServerInterceptor
    LogGrpcInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }
}
