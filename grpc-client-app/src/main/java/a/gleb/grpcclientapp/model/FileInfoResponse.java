package a.gleb.grpcclientapp.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Information about file on server")
public class FileInfoResponse {

    @Schema(description = "File name")
    private String fileName;

    @Schema(description = "File extension, file type")
    private String fileType;

    @Schema(description = "Date when file was create or edit last time")
    private String dateOfCreation;
}
