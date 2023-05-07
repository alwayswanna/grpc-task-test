package a.gleb.grpcserverapp.stub;

import a.gleb.grpcserverapp.proto.Image.*;
import a.gleb.grpcserverapp.proto.ImageServiceGrpc.ImageServiceImplBase;
import a.gleb.grpcserverapp.service.FileOperationService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * GRPC-stub for make operation with files
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FileRpcStub extends ImageServiceImplBase {

    private final FileOperationService fileOperationService;

    @Override
    public StreamObserver<ImageRequest> uploadImage(StreamObserver<ImageResponse> responseObserver) {
        log.info("Start save file to directory");
        return fileOperationService.saveFileInStorage(responseObserver);
    }

    @Override
    public void checkFiles(FileRequest request, StreamObserver<FileResponse> responseObserver) {
        log.info("Start get information about files in folder");
        fileOperationService.getFilesInformation(responseObserver);
        log.info("End get information about files in folder");
    }

    @Override
    public void downloadFile(DownloadFileRequest request, StreamObserver<DownloadFileResponse> responseObserver) {
        log.info("Start download file, [filename={}]", request.getFilename());
        fileOperationService.downloadFile(request, responseObserver);
        log.info("End download file, [filename={}]", request.getFilename());
    }
}
