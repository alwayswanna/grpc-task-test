package a.gleb.webgrpcclient.service;

import a.gleb.service.GreetingServiceGrpc;
import a.gleb.service.Radio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TextServiceImpl {

    private final GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub;

    @Autowired
    public TextServiceImpl(GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public String simpleTextRequest(String message){
        Radio.HelloRequest request = Radio.HelloRequest
                .newBuilder()
                .setName(message)
                .build();
        Radio.HelloResponse response = blockingStub.greeting(request);
        return response.toString();
    }
}
