package io.eaglejs.ffxi.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OpenAPIDefinition(
    info = @Info(
        title = "FFXI Player Service API",
        version = "1.0",
        description = "API for FFXI Player Service - provides health monitoring and player data."
    )
)
public class SwaggerConfig implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        OpenAPI openAPI = new OpenAPI();
        SwaggerConfiguration swaggerConfig = new SwaggerConfiguration()
                .openAPI(openAPI)
                .resourcePackages(Stream.of("io.eaglejs.ffxi.resources").collect(Collectors.toSet()));

        OpenApiResource openApiResource = new OpenApiResource();
        openApiResource.openApiConfiguration(swaggerConfig);
        context.register(openApiResource);
        return true;
    }
}
