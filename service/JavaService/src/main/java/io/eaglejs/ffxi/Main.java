package io.eaglejs.ffxi;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.eaglejs.ffxi.config.SwaggerConfig;
import io.eaglejs.ffxi.health.MongoHealthCheck;
import io.eaglejs.ffxi.resources.HealthResource;
import io.eaglejs.ffxi.resources.PlayersResource;
import io.eaglejs.ffxi.resources.SinglePlayerResource;
import io.eaglejs.ffxi.service.MongoDBService;
import io.eaglejs.ffxi.websocket.WebSocketManager;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.io.IOException;
import java.util.EnumSet;

public class Main extends Application<FFXIConfiguration> {

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    public String getName() {
        return "ffxi-player-service";
    }

    @Override
    public void initialize(Bootstrap<FFXIConfiguration> bootstrap) {
        // initialization logic
    }

    @Override
    public void run(FFXIConfiguration configuration, Environment environment) {
        // Configure CORS if enabled
        if (configuration.getCors().isEnabled()) {
            configureCors(environment, configuration.getCors());
        }
        
        // Initialize MongoDB service
        MongoDBService mongoDBService = new MongoDBService(configuration.getMongoUri());
        
        environment.healthChecks().register("mongodb", new MongoHealthCheck(configuration.getMongoUri()));
        environment.jersey().register(new HealthResource(environment.healthChecks()));
        environment.jersey().register(new PlayersResource(mongoDBService));
        environment.jersey().register(new SinglePlayerResource(mongoDBService));
        environment.jersey().register(new SwaggerConfig());

        // Register WebSocket endpoint
        ServletContextHandler contextHandler = environment.getApplicationContext();
        environment.lifecycle().manage(new WebSocketManager(contextHandler));

        // SPA fallback filter: forwards unknown paths to index.html for Vue Router
        FilterRegistration.Dynamic spaFilter = environment.servlets().addFilter("spaFilter", new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String path = httpRequest.getRequestURI();

                // Pass through API routes, WebSocket, and any path with a file extension
                if (path.startsWith("/api/") || path.startsWith("/ws/") || path.contains(".")) {
                    chain.doFilter(request, response);
                    return;
                }

                // Forward SPA routes (e.g. /charts, /players/123) to index.html
                request.getRequestDispatcher("/index.html").forward(request, response);
            }

            @Override public void init(FilterConfig config) {}
            @Override public void destroy() {}
        });
        spaFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");

        // Configure static assets serving for UI
        // API endpoints are served at /api/* (configured in config.yml)
        // Static UI files are served at the root /*
        ServletHolder staticServlet = new ServletHolder("static", DefaultServlet.class);
        staticServlet.setInitParameter("resourceBase",
            Main.class.getClassLoader().getResource("assets").toExternalForm());
        staticServlet.setInitParameter("dirAllowed", "false");
        staticServlet.setInitParameter("pathInfoOnly", "false");
        contextHandler.addServlet(staticServlet, "/*");
    }
    
    private void configureCors(Environment environment, io.eaglejs.ffxi.config.CorsConfiguration corsConfig) {
        final FilterRegistration.Dynamic cors = 
            environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        
        // Configure CORS parameters from configuration
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, corsConfig.getAllowedOrigins());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, corsConfig.getAllowedHeaders());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, corsConfig.getAllowedMethods());
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, String.valueOf(corsConfig.isAllowCredentials()));
        cors.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, corsConfig.getExposedHeaders());
        
        // Add URL mapping for CORS filter
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
