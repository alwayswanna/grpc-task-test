import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class ClientWebApplication {

    public static void main(String[] args) {
        SpringBootApplication.run(ClientWebApplication.class, args);
        System.out.println("done");
    }
}
