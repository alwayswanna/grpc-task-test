package a.gleb.grpcserverapp.service;

import a.gleb.grpcserverapp.configuration.properties.GrpcServerConfigurationProperties;
import a.gleb.grpcserverapp.exception.FileOperationException;
import a.gleb.grpcserverapp.proto.Image.*;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

import static io.grpc.Status.ABORTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileOperationService {

    private static final int SUCCESS_CODE = 200;
    private static final String SUCCESS_MESSAGE = "File successfully uploaded";

    private final GrpcServerConfigurationProperties properties;

    /**
     * Method saves file to storage
     *
     * @param responseObserver stream with file bytes & file metadata
     */
    public StreamObserver<ImageRequest> saveFileInStorage(StreamObserver<ImageResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ImageRequest value) {
                var bytes = value.getData().toByteArray();
                try (var bOS =
                             new BufferedOutputStream(
                                     new FileOutputStream(
                                             String.format("%s/%s", properties.getDirectoryToSave(), value.getName())
                                     )
                             )
                ) {
                    bOS.write(bytes);
                    bOS.flush();
                } catch (IOException e) {
                    log.error("Error while save file from request. [filename={}, error=]", value.getName(), e);
                    throw new FileOperationException(
                            String.format("Error while save new file. [filename=%s]", value.getName()), e
                    );
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error while proceed uploadImage request. Cause:", t.getCause());
            }

            @Override
            public void onCompleted() {
                log.info("Successfully end proceed uploadImage RPC-call.");
                responseObserver.onNext(
                        ImageResponse
                                .newBuilder()
                                .setStatus(SUCCESS_CODE)
                                .setMessage(SUCCESS_MESSAGE)
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * Method returns information about files which stored in server directory
     *
     * @param responseObserver stream for return file information
     */
    public void getFilesInformation(StreamObserver<FileResponse> responseObserver) {
        var fileResponse = FileResponse.newBuilder();
        var serverDirectory = new File(properties.getDirectoryToSave());

        if (serverDirectory.isDirectory()) {
            for (var image : Objects.requireNonNull(serverDirectory.listFiles())) {
                FileTime creationTime = null;
                var extension = image.getName().substring(image.getName().lastIndexOf(".") + 1);
                try {
                    creationTime = (FileTime) Files.getAttribute(Path.of(image.getPath()), "creationTime");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                var fileInfo = FileInfo.newBuilder()
                        .setFilename(image.getName())
                        .setFiletype(extension)
                        .setDateCreation(creationTime == null ? "" : creationTime.toString())
                        .build();
                fileResponse.addFileInfo(fileInfo);
            }
        }

        var response = fileResponse.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Method download send file from directory to StreamObserver
     *
     * @param request          which contains filename
     * @param responseObserver stream for response
     */
    public void downloadFile(DownloadFileRequest request, StreamObserver<DownloadFileResponse> responseObserver) {
        var file = new File(String.format("/var/tmp/%s", request.getFilename()));

        try (var ins = new FileInputStream(file)){
            byte[] bytes = ins.readAllBytes();
            BufferedInputStream imageStream = new BufferedInputStream(new ByteArrayInputStream(bytes));

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length;

            while ((length = imageStream.read(buffer, 0, bufferSize)) != -1) {
                responseObserver.onNext(DownloadFileResponse.newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, length))
                        .setSize(bufferSize)
                        .build());
            }

            imageStream.close();
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while get file bytes and put it into stream", e);
            responseObserver.onError(ABORTED
                    .withDescription("Unable to acquire the file " + request.getFilename())
                    .withCause(e)
                    .asException());
        }
    }
}
