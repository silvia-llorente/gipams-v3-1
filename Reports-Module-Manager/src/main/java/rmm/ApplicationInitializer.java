package rmm;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import rmm.Tasks.*;

public class ApplicationInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        KeycloakMetricsManager.init();
        ThreatsDetector.init();
        System.out.println("Servlet context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        KeycloakMetricsManager.finish();
        ThreatsDetector.finish();
        System.out.println("Servlet context destroyed");
    }
}
