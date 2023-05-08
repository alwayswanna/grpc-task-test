package a.gleb.grpcserverapp.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogGrpcInterceptor implements ServerInterceptor {

    @Override
    public <Request, Response> Listener<Request> interceptCall(
            ServerCall<Request, Response> call,
            Metadata metadata,
            ServerCallHandler<Request, Response> callHandler
    ) {
        log.info(call.getMethodDescriptor().getFullMethodName());
        return callHandler.startCall(call, metadata);
    }
}
