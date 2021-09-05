package rpc;

import a.gleb.service.GreetingServiceGrpc;
import a.gleb.service.Radio;
import io.grpc.stub.StreamObserver;

public class ServiceRpcImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    @Override
    public void greeting(Radio.HelloRequest request, StreamObserver<Radio.HelloResponse> responseObserver) {
        Radio.HelloResponse response = Radio.HelloResponse.newBuilder()
                .setGreeting("User send message to server: " + request.getName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
