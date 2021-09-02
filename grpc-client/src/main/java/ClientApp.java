import a.gleb.service.GreetingServiceGrpc;
import a.gleb.service.Service;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class ClientApp {

    private static final int PORT = 50051;

    private final ManagedChannel channel;
    private final GreetingServiceGrpc.GreetingServiceBlockingStub stub;
    private final GreetingServiceGrpc.GreetingServiceStub async_stub;


    public ClientApp(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    ClientApp(ManagedChannel channel) {
        this.channel = channel;
        stub = GreetingServiceGrpc.newBlockingStub(channel);
        async_stub = GreetingServiceGrpc.newStub(channel);

    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void startStream(final String filepath) {
        StreamObserver<Service.ImageResponse> responseObserver = new StreamObserver<Service.ImageResponse>() {
            @Override
            public void onNext(Service.ImageResponse value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        StreamObserver<Service.ImageRequest> requestObserver = async_stub.uploadImage(responseObserver);
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                System.out.println("File does not exist");
                return;
            }
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                int buffer_size = 512 * 1024;
                byte[] buffer = new byte[buffer_size];
                int size = 0;

                while ((size = bufferedInputStream.read(buffer)) > 0) {
                    ByteString byteString = ByteString.copyFrom(buffer, 0, size);
                    var request = Service.ImageRequest.newBuilder().setName(filepath).setData(byteString).setOffset(size).build();
                    requestObserver.onNext(request);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (RuntimeException rE) {
            rE.printStackTrace();
            throw rE;
        }
        requestObserver.onCompleted();
    }

    private void sendSimpleRequest() {
        Service.HelloRequest request = Service.HelloRequest
                .newBuilder()
                .setName("client_name")
                .build();

        Service.HelloResponse response = stub.greeting(request);
        System.out.println(response);

    }

    public static void main(String[] args) throws InterruptedException {
        ClientApp clientApp = new ClientApp("localhost", 50051);
        try {
            clientApp.startStream("C:\\Users\\agleb\\Pictures\\test.png");
            clientApp.sendSimpleRequest();
        } finally {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clientApp.shutdown();
        }

    }

}
