package a.gleb.grpcclientapp.mapper;

import a.gleb.grpcclientapp.model.FileInfoResponse;
import a.gleb.grpcclientapp.model.FileServerResponse;
import a.gleb.grpcserverapp.proto.Image.FileInfo;
import a.gleb.grpcserverapp.proto.Image.FileResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileResponseMapper {

    public FileServerResponse toFileServerResponse(FileResponse response) {
        return FileServerResponse.builder()
                .fileInfos(this.mapToCollectionOfFileInfoResponse(response.getFileInfoList()))
                .build();
    }

    private List<FileInfoResponse> mapToCollectionOfFileInfoResponse(List<FileInfo> fileInfos) {
        return fileInfos.stream()
                .map(it -> FileInfoResponse.builder()
                        .fileName(it.getFilename())
                        .fileType(it.getFiletype())
                        .dateOfCreation(it.getDateCreation())
                        .build()
                )
                .toList();
    }
}
