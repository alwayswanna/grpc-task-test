package a.gleb.grpcclientapp.controller;

import a.gleb.grpcclientapp.model.FileServerResponse;
import a.gleb.grpcclientapp.service.FileOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file-operation")
@Tag(name = GrpcFileClientController.FILE_CONTROLLER_TAG)
public class GrpcFileClientController {

    static final String FILE_CONTROLLER_TAG = "file-controller-tag";

    private final FileOperationService fileOperationService;

    @Operation(summary = "Method returns information about loaded files")
    @ApiResponses(value = {
            @ApiResponse(description = "OK", responseCode = "200"),
            @ApiResponse(description = "Internal server error", responseCode = "500")
    })
    @GetMapping("/files")
    public Mono<FileServerResponse> getFileInformation() {
        return fileOperationService.getFileInformation();
    }

    @Operation(summary = "Download file by name")
    @ApiResponses(value = {
            @ApiResponse(description = "OK", responseCode = "200"),
            @ApiResponse(description = "Internal server error", responseCode = "500")
    })
    @GetMapping("/file")
    public ResponseEntity<Mono<Resource>> downloadFile(@RequestParam String fileName) {
        return ok()
                .header(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(fileOperationService.downloadFile(fileName));
    }

    @Operation(summary = "Upload new file")
    @ApiResponses(value = {
            @ApiResponse(description = "OK", responseCode = "200"),
            @ApiResponse(description = "Internal server error", responseCode = "500")
    })
    @PostMapping(value = "/file-upload", consumes = MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, String>>> uploadFile(@RequestPart(name = "file") Mono<FilePart> multipartFile) {
        return multipartFile
                .flatMap(part -> fileOperationService.uploadFile(part.content(), part.filename()))
                .map(message -> ok().body(Map.of("status", message)));
    }
}
