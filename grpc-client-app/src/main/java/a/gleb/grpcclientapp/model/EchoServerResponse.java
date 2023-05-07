package a.gleb.grpcclientapp.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Response from Grpc-server")
public class EchoServerResponse {

    @Schema(description = "Message from server")
    private String serverResponse;
}
