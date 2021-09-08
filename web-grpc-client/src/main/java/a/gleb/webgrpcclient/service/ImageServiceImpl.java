package a.gleb.webgrpcclient.service;

import a.gleb.service.Image;
import a.gleb.service.ImageServiceGrpc;
import a.gleb.webgrpcclient.models.FileInformation;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ImageServiceImpl {

    private final ImageServiceGrpc.ImageServiceStub asyncStub;
    private final ImageServiceGrpc.ImageServiceBlockingStub blockingStubImage;

    @Autowired
    public ImageServiceImpl(ImageServiceGrpc.ImageServiceStub asyncStub, ImageServiceGrpc.ImageServiceBlockingStub blockingStubImage) {
        this.asyncStub = asyncStub;
        this.blockingStubImage = blockingStubImage;
    }

    public void sendImage(final InputStream fileInputStream, String filename) {
        StreamObserver<Image.ImageResponse> responseStreamObserver = new StreamObserver<Image.ImageResponse>() {
            @Override
            public void onNext(Image.ImageResponse value) {
                log.info("Success: 200. " + new Date());
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error: 500. " + new Date());
            }

            @Override
            public void onCompleted() {
                log.info("Send message method complete. " + new Date());
            }
        };

        StreamObserver<Image.ImageRequest> requestObserver = asyncStub.uploadImage(responseStreamObserver);
        try {
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                int buffer_size = 512 * 1024;
                byte[] buffer = new byte[buffer_size];
                int size = 0;

                while ((size = bufferedInputStream.read(buffer)) > 0) {
                    ByteString byteString = ByteString.copyFrom(buffer, 0, size);
                    var request = Image.ImageRequest.newBuilder().setName(filename).setData(byteString).setOffset(size).build();
                    requestObserver.onNext(request);
                }
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        } catch (RuntimeException ex) {
            log.warn(ex.getMessage());
        }
        requestObserver.onCompleted();
    }


    public List<FileInformation> checkServerDirectory(final String message) {
        List<FileInformation> fileInformationList = new ArrayList<>();
        Image.FileRequest request = Image.FileRequest
                .newBuilder()
                .setMessage(message)
                .build();
        Image.FileResponse response = blockingStubImage.checkFiles(request);
        for (int count = 0; count < response.getFileInfoCount(); count++) {
            Image.FileInfo fileInfo = response.getFileInfo(count);
            FileInformation fileInformation = new FileInformation(fileInfo.getFilename(),
                    fileInfo.getFiletype(), fileInfo.getDateCreation());
            fileInformationList.add(fileInformation);
        }
        return fileInformationList;
    }

    public ByteArrayOutputStream downloadFile(final String filename) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);

        StreamObserver<Image.DownloadFileResponse> streamObserver = new StreamObserver<Image.DownloadFileResponse>() {
            @Override
            public void onNext(Image.DownloadFileResponse dataResponse) {
                try {
                    byteArrayOutputStream.write(dataResponse.getData().toByteArray());
                } catch (IOException e) {
                    log.warn(e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                log.warn("DownloadFileGrpc: server error: ", t);
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("DownloadFileGrpc: completed!");
                completed.compareAndSet(false, true);
                countDownLatch.countDown();
            }
        };
        try{
            Image.DownloadFileRequest request = Image.DownloadFileRequest
                    .newBuilder()
                    .setFilename(filename)
                    .build();

            asyncStub.downloadFile(request, streamObserver);

            countDownLatch.await(5, TimeUnit.MINUTES);

            if (!completed.get()){
                throw  new Exception("DownloadFileGrpc: does not complete");
            }
        }catch (Exception e){
            log.warn("DownloadFileGrpc: does not complete" + e.getMessage());
        }

        return byteArrayOutputStream;
    }

}
