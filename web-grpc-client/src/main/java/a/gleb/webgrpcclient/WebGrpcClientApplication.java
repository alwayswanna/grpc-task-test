package a.gleb.webgrpcclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebGrpcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebGrpcClientApplication.class, args);
    }
    //TODO: create restrictions on uploading and downloading images
}
