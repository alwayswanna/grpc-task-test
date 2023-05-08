package a.gleb.grpcclientapp.service;

import a.gleb.grpcclientapp.model.EchoServerResponse;
import a.gleb.grpcserverapp.proto.Echo.HelloRequest;
import a.gleb.grpcserverapp.proto.GreetingServiceGrpc.GreetingServiceBlockingStub;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EchoService {

    @GrpcClient("local-grpc-server")
    private GreetingServiceBlockingStub greetingServiceGrpc;

    /**
     * Server sends message to GRPC-server
     *
     * @param message from request which need send to server
     * @return {@link EchoServerResponse} with server response data
     */
    public Mono<EchoServerResponse> echoRequest(@NonNull String message) {
        var response = greetingServiceGrpc.greeting(
                HelloRequest.newBuilder()
                        .setMessage(message)
                        .build()
        );

        return Mono.just(
                EchoServerResponse.builder()
                        .serverResponse(response.getMessage())
                        .build()
        );
    }

}
