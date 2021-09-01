package rpc;

import a.gleb.service.GreetingServiceGrpc;
import a.gleb.service.Service;
import io.grpc.stub.StreamObserver;

public class ServiceRpcImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    @Override
    public void greeting(Service.HelloRequest request, StreamObserver<Service.HelloResponse> responseObserver) {
        System.out.println(request);

        Service.HelloResponse response = Service.HelloResponse.newBuilder()
                .setGreeting("Response from server to: " + request.getName())
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }
}
