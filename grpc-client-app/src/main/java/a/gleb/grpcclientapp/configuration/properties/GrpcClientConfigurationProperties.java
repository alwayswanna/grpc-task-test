package a.gleb.grpcclientapp.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ConfigurationProperties("client-config")
public class GrpcClientConfigurationProperties {

    @NotNull
    private ExecutorConfig executorConfig;

    private int maxUploadTimeout = 5;

    @Getter
    @Setter
    public static class ExecutorConfig {

        @NotNull
        private Integer corePoolSize;

        @NotNull
        private Integer maxPoolSize;

        @NotNull
        private Long keepAliveInMinutes;
    }
}
