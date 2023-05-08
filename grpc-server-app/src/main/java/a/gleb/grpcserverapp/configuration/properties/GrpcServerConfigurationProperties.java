package a.gleb.grpcserverapp.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ConfigurationProperties("server-config")
public class GrpcServerConfigurationProperties {

    @NotNull
    private String directoryToSave;
}
