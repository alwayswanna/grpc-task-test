import io.grpc.Server;
import io.grpc.ServerBuilder;
import rpc.ServiceRpcImpl;

import java.io.IOException;

public class ServerApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server starting ...");

        Server server = ServerBuilder.forPort(50051)
                .addService(new ServiceRpcImpl())
                .build();

        server.start();

        System.out.println("Server start!");

        server.awaitTermination();
    }
}
