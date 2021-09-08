package rpc;

import a.gleb.service.Image;
import a.gleb.service.ImageServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;

public class ImageServiceImpl extends ImageServiceGrpc.ImageServiceImplBase {

    private final int mStatus = 200;
    private final String message = "ImageUpload: DONE";
    private BufferedOutputStream bufferedOutputStream = null;
    private static final String SERVER_DIRECTORY = "D:\\GrpcServerCache\\";

    @Override
    public StreamObserver<Image.ImageRequest> uploadImage(StreamObserver<Image.ImageResponse> responseObserver) {
        return new StreamObserver<Image.ImageRequest>() {
            @Override
            public void onNext(Image.ImageRequest request) {
                byte[] data = request.getData().toByteArray();
                long offset = request.getOffset();
                String name = request.getName();
                System.out.println(name);
                try {
                    if (bufferedOutputStream == null) {
                        bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(SERVER_DIRECTORY + name));
                    }
                    bufferedOutputStream.write(data);
                    bufferedOutputStream.flush();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Image.ImageResponse
                        .newBuilder()
                        .setStatus(mStatus)
                        .setMessage(message)
                        .build()
                );
                responseObserver.onCompleted();

                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bufferedOutputStream = null;
                }
            }
        };
    }

    @Override
    public void checkFiles(Image.FileRequest request, StreamObserver<Image.FileResponse> responseObserver) {
        Image.FileResponse.Builder fileResponse = Image.FileResponse.newBuilder();
        File serverDirectory = new File("D:\\GrpcServerCache");
        if (serverDirectory.isDirectory()) {
            for (File image : serverDirectory.listFiles()) {
                FileTime creationTime = null;
                String extension = image.getName().substring(image.getName().lastIndexOf(".") + 1);
                try {
                    creationTime = (FileTime) Files.getAttribute(Path.of(image.getPath()), "creationTime");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert creationTime != null;
                Image.FileInfo fileInfo = Image.FileInfo.newBuilder()
                        .setFilename(image.getName())
                        .setFiletype(extension)
                        .setDateCreation(creationTime.toString())
                        .build();
                fileResponse.addFileInfo(fileInfo);
            }
            System.out.println("File list request message: " + request.getMessage());

            Image.FileResponse resultResponse = fileResponse.build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void downloadFile(Image.DownloadFileRequest request, StreamObserver<Image.DownloadFileResponse> responseObserver) {
        try{
            String filename = request.getFilename();
            InputStream inputStream = new FileInputStream(SERVER_DIRECTORY + filename);
            byte[] bytes = inputStream.readAllBytes();
            BufferedInputStream imageStream = new BufferedInputStream(new ByteArrayInputStream(bytes));

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = imageStream.read(buffer, 0, bufferSize) ) != -1) {
                responseObserver.onNext(Image.DownloadFileResponse
                        .newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, length))
                        .setSize(bufferSize)
                        .build());
            }
            imageStream.close();
            responseObserver.onCompleted();
        }catch (Throwable e){
            responseObserver.onError(Status.ABORTED
                    .withDescription("DownloadFileFromGrpc: occurred error " + request.getFilename())
                    .withCause(e)
                    .asException());
        }
    }
}
