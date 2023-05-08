package a.gleb.grpcclientapp.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogGrpcInterceptor implements ClientInterceptor {

    /**
     *  <p>
     *      From examples:
     *       <href><a href="https://github.com/yidongnan/grpc-spring-boot-starter/tree/master">starter-examples</a></href>
     *  </p>
     */
    @Override
    public <Request, Response> ClientCall<Request, Response> interceptCall(
            MethodDescriptor<Request, Response> method,
            CallOptions options,
            Channel channel
    ) {
        log.info("Received call to {}", method.getFullMethodName());
        return new ForwardingClientCall.SimpleForwardingClientCall<>(channel.newCall(method, options)) {

            @Override
            public void sendMessage(Request message) {
                log.debug("Request message: {}", message);
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<Response> responseListener, Metadata headers) {
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                            @Override
                            public void onMessage(Response message) {
                                log.debug("Response message: {}", message);
                                super.onMessage(message);
                            }

                            @Override
                            public void onHeaders(Metadata headers) {
                                log.debug("gRPC headers: {}", headers);
                                super.onHeaders(headers);
                            }

                            @Override
                            public void onClose(Status status, Metadata trailers) {
                                log.info("Interaction ends with status: {}", status);
                                log.info("Trailers: {}", trailers);
                                super.onClose(status, trailers);
                            }
                        },
                        headers);
            }
        };
    }

}
