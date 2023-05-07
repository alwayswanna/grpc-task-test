package a.gleb.grpcclientapp.service;

import a.gleb.grpcclientapp.configuration.properties.GrpcClientConfigurationProperties;
import a.gleb.grpcclientapp.exception.FileServerException;
import a.gleb.grpcclientapp.mapper.FileResponseMapper;
import a.gleb.grpcclientapp.model.FileServerResponse;
import a.gleb.grpcserverapp.proto.Image.*;
import a.gleb.grpcserverapp.proto.ImageServiceGrpc.ImageServiceFutureStub;
import a.gleb.grpcserverapp.proto.ImageServiceGrpc.ImageServiceStub;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileOperationService {

    private static final String SUCCESS_CODE = "Success";

    @GrpcClient("local-grpc-server")
    private ImageServiceFutureStub imageFutureStubService;
    @GrpcClient("local-grpc-server")
    private ImageServiceStub imageServiceStub;

    private final ExecutorService taskExecutor;
    private final FileResponseMapper fileResponseMapper;
    private final GrpcClientConfigurationProperties properties;

    /**
     * Method get information about loaded files.
     *
     * @return {@link FileServerResponse} response from server which contain file information
     */
    public Mono<FileServerResponse> getFileInformation() {
        return Mono.create(fileInfoSink -> Futures.addCallback(
                        imageFutureStubService.checkFiles(FileRequest.newBuilder().build()),
                        new FutureCallback<>() {

                            @Override
                            public void onSuccess(FileResponse result) {
                                fileInfoSink.success(fileResponseMapper.toFileServerResponse(result));
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                log.error("Error while get file information", t);
                                throw new FileServerException("Error while get information about file on server");
                            }
                        },
                        taskExecutor
                )
        );
    }

    /**
     * Download file with name from request
     *
     * @param fileName name of file
     * @return {@link Resource} stream of bytes
     */
    public Mono<Resource> downloadFile(@NonNull String fileName) {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var countDownLatch = new CountDownLatch(1);
        var isCompleted = new AtomicBoolean(false);

        StreamObserver<DownloadFileResponse> streamObserver = new StreamObserver<>() {
            @Override
            public void onNext(DownloadFileResponse dataResponse) {
                try {
                    byteArrayOutputStream.write(dataResponse.getData().toByteArray());
                } catch (IOException e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error while download file from server, [filename={}]", fileName, t);
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Download file is completed, [filename={}]", fileName);
                isCompleted.compareAndSet(false, true);
                countDownLatch.countDown();
            }
        };

        try {
            imageServiceStub.downloadFile(
                    DownloadFileRequest
                            .newBuilder()
                            .setFilename(fileName)
                            .build(),
                    streamObserver
            );

            var res = countDownLatch.await(properties.getMaxUploadTimeout(), MINUTES);

            if (!isCompleted.get() || !res) {
                throw new FileServerException("File does not downloaded");
            }
        } catch (InterruptedException interruptedException) {
            streamObserver.onError(interruptedException);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            streamObserver.onError(e);
        }

        return Mono.just(new ByteArrayResource(byteArrayOutputStream.toByteArray()));
    }

    /**
     * Upload file to server
     *
     * @param content  file bytes
     * @param filename name of file
     */
    public Mono<String> uploadFile(Flux<DataBuffer> content, String filename) {
        StreamObserver<ImageResponse> responseStreamObserver = new StreamObserver<>() {
            @Override
            public void onNext(ImageResponse value) {
                log.info("File successfully uploaded to server, [filename={}]", filename);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error while upload file to server, [filename={}]", filename);
            }

            @Override
            public void onCompleted() {
                log.info("File uploaded is completed, [filename={}]", filename);
            }
        };

        return content.flatMap(dataBuffer -> Flux.just(dataBuffer.asByteBuffer().array()))
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map(bytes -> {
                    var requestObserver = imageServiceStub.uploadImage(responseStreamObserver);
                    var ins = new ByteArrayInputStream(
                            bytes.stream()
                                    .findFirst()
                                    .orElseThrow(() -> new FileServerException("Error while read file bytes from bytes"))
                    );

                    try (var bufferedInputStream = new BufferedInputStream(ins)) {
                        int bufferSize = 512 * 1024;
                        byte[] buffer = new byte[bufferSize];
                        int size = 0;

                        while ((size = bufferedInputStream.read(buffer)) > 0) {
                            requestObserver.onNext(
                                    ImageRequest.newBuilder()
                                            .setName(filename)
                                            .setData(ByteString.copyFrom(buffer, 0, size))
                                            .setOffset(size)
                                            .build()
                            );
                        }
                    } catch (IOException e) {
                        requestObserver.onError(e);
                    }

                    requestObserver.onCompleted();

                    return SUCCESS_CODE;
                });
    }
}
