import a.gleb.service.GreetingServiceGrpc;
import a.gleb.service.Service;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClientApp {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:50051")
                .usePlaintext()
                .build();

        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

        Service.HelloRequest request = Service.HelloRequest
                .newBuilder()
                .setName("ClientName")
                .build();

        Service.HelloResponse response = stub.greeting(request);
        System.out.println("Client send request. Returned response: " + response);

        channel.shutdownNow();
    }
}
