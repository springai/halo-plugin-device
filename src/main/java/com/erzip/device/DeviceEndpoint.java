package com.erzip.device;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.erzip.device.extension.Device;
import com.erzip.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;

@Component
@RequiredArgsConstructor
public class DeviceEndpoint implements CustomEndpoint {

    private final DeviceService deviceService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "console.api.device.erzip.com/v1alpha1/Device";
        return route()
            .GET("devices", this::listDevice,
                builder -> {
                    builder.operationId("ListDevices")
                        .description("List devices.")
                        .tag(tag)
                        .response(responseBuilder().implementation(
                            ListResult.generateGenericClass(Device.class)));

                    DeviceQuery.buildParameters(builder);
                }
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.device.erzip.com/v1alpha1");
    }

    private Mono<ServerResponse> listDevice(ServerRequest serverRequest) {
        DeviceQuery query = new DeviceQuery(serverRequest.queryParams());
        return deviceService.listDevice(query).flatMap(
            devices -> ServerResponse.ok().bodyValue(devices));
    }
}
