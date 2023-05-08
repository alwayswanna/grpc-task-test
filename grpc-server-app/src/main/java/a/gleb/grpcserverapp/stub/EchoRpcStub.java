package a.gleb.grpcserverapp.stub;

import a.gleb.grpcserverapp.proto.Echo.HelloRequest;
import a.gleb.grpcserverapp.proto.Echo.HelloResponse;
import a.gleb.grpcserverapp.proto.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class EchoRpcStub extends GreetingServiceGrpc.GreetingServiceImplBase {

    private static final String USER_MESSAGE = "User sends message: %s";

    @Override
    public void greeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        var response = HelloResponse.newBuilder()
                .setMessage(String.format(USER_MESSAGE, request.getMessage()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
