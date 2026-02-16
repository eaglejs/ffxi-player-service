package io.eaglejs.ffxi;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.eaglejs.ffxi.config.SwaggerConfig;
import io.eaglejs.ffxi.health.MongoHealthCheck;
import io.eaglejs.ffxi.resources.HealthResource;
import io.eaglejs.ffxi.websocket.WebSocketManager;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;

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
        environment.healthChecks().register("mongodb", new MongoHealthCheck(configuration.getMongoUri()));
        environment.jersey().register(new HealthResource(environment.healthChecks()));
        environment.jersey().register(new SwaggerConfig());

        // Register WebSocket endpoint
        ServletContextHandler contextHandler = environment.getApplicationContext();
        environment.lifecycle().manage(new WebSocketManager(contextHandler));

        // Configure static assets serving
        ServletHolder staticServlet = new ServletHolder("static", DefaultServlet.class);
        staticServlet.setInitParameter("resourceBase", 
            Main.class.getClassLoader().getResource("assets").toExternalForm());
        staticServlet.setInitParameter("dirAllowed", "true");
        staticServlet.setInitParameter("pathInfoOnly", "true");
        contextHandler.addServlet(staticServlet, "/api/*");
    }
}
