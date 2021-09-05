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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void sendImage(final InputStream fileInputStream, String filename){
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
        try{
            System.out.println(filename);
            try{
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                int buffer_size = 512 * 1024;
                byte[] buffer = new byte[buffer_size];
                int size = 0;

                while ((size = bufferedInputStream.read(buffer)) > 0){
                    ByteString byteString = ByteString.copyFrom(buffer, 0, size);
                    var request = Image.ImageRequest.newBuilder().setName(filename).setData(byteString).setOffset(size).build();
                    requestObserver.onNext(request);
                }
            }catch (IOException e){
                log.warn(e.getMessage());
            }
        }catch (RuntimeException ex){
            log.warn(ex.getMessage());
        }
        requestObserver.onCompleted();
    }


    public List<FileInformation> checkServerDirectory(final String message){
       List<FileInformation> fileInformationList = new ArrayList<>();
        Image.FileRequest request = Image.FileRequest
                .newBuilder()
                .setMessage(message)
                .build();
        Image.FileResponse response = blockingStubImage.checkFiles(request);
        for (int count = 0; count < response.getFileInfoCount(); count++){
            Image.FileInfo fileInfo = response.getFileInfo(count);
            FileInformation fileInformation = new FileInformation(fileInfo.getFilename(),
                    fileInfo.getFiletype(), fileInfo.getDateCreation());
            fileInformationList.add(fileInformation);
        }
        for (FileInformation inf : fileInformationList){
            System.out.println(inf);
        }
        return fileInformationList;
    }

}
