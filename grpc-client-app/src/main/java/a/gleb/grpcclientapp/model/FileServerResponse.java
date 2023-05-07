package a.gleb.grpcclientapp.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Response with file information on grpc-server")
public class FileServerResponse {

    @Schema(description = "List with information about files on grpc-server")
    List<FileInfoResponse> fileInfos;
}
