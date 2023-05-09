package a.gleb.grpcclientapp.controller;

import a.gleb.grpcclientapp.model.EchoServerResponse;
import a.gleb.grpcclientapp.service.EchoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static a.gleb.grpcclientapp.controller.GrpcEchoClientController.ECHO_CONTROLLER_TAG;

@RestController
@RequiredArgsConstructor
@Tag(name = ECHO_CONTROLLER_TAG)
@RequestMapping("/api/v1/echo")
public class GrpcEchoClientController {

    static final String ECHO_CONTROLLER_TAG = "echo-controller-tag";

    private final EchoService echoService;

    @Operation(summary = "Send echo message to grpc-server")
    @ApiResponses(value = {
            @ApiResponse(description = "OK", responseCode = "200"),
            @ApiResponse(description = "Internal server error", responseCode = "500")
    })
    @GetMapping("/send")
    public Mono<EchoServerResponse> echo(@RequestParam String message) {
        return echoService.echoRequest(message);
    }
}
