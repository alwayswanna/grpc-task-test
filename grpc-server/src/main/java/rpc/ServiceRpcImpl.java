package rpc;

import a.gleb.service.GreetingServiceGrpc;
import a.gleb.service.Service;
import io.grpc.stub.StreamObserver;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServiceRpcImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
    private final int mStatus = 200;
    private final String message = "ImageUpload: DONE";
    private BufferedOutputStream bufferedOutputStream = null;


    @Override
    public void greeting(Service.HelloRequest request, StreamObserver<Service.HelloResponse> responseObserver) {
        System.out.println("greeting request: " + request);

        Service.HelloResponse response = Service.HelloResponse.newBuilder()
                .setGreeting("Response from server to: " + request.getName())
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Service.ImageRequest> uploadImage(StreamObserver<Service.ImageResponse> responseObserver) {
        int count = 0;
        return new StreamObserver<Service.ImageRequest>() {
            @Override
            public void onNext(Service.ImageRequest request) {
                byte[] data = request.getData().toByteArray();
                long offset = request.getOffset();
                String name = request.getName();
                System.out.println(name);
                try {
                    if (bufferedOutputStream == null) {
                        bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("D:\\test.png"));
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
                responseObserver.onNext(Service.ImageResponse
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
}
