package dk.dbc.opencat.connector;

import dk.dbc.httpclient.HttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;

/**
 * OpencatBusinessConnector factory
 * <p>
 * Synopsis:
 * </p>
 * <pre>
 *    // New instance
 *    OpencatBusinessConnector obc = OpencatBusinessConnector.create("http://opencat-business-service");
 *
 *    // Singleton instance in CDI enabled environment
 *    {@literal @}Inject
 *    OpencatBusinessConnectorFactory factory;
 *    ...
 *    OpencatBusinessConnector rsc = factory.getInstance();
 *
 *    // or simply
 *    {@literal @}Inject
 *    OpencatBusinessConnector rsc;
 * </pre>
 * <p>
 * CDI case depends on the opencat-business baseurl being defined as
 * the value of either a system property or environment variable
 * named OPENCAT_BUSINESS_URL. OPENCAT_BUSINESS_TIMING_LOG_LEVEL
 * should be one of TRACE, DEBUG, INFO(default), WARN or ERROR, for setting
 * log level
 * </p>
 */
@ApplicationScoped
public class OpencatBusinessConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpencatBusinessConnectorFactory.class);

    public static OpencatBusinessConnector create(String opencatBusinessBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating OpencatBusinessConnector for: {}", opencatBusinessBaseUrl);
        return new OpencatBusinessConnector(client, opencatBusinessBaseUrl);
    }

    public static OpencatBusinessConnector create(String opencatBusinessBaseUrl, OpencatBusinessConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating OpencatBusinessConnector for: {}", opencatBusinessBaseUrl);
        return new OpencatBusinessConnector(client, opencatBusinessBaseUrl, level);
    }

    @Inject
    @ConfigProperty(name = "OPENCAT_BUSINESS_URL")
    private String opencatBusinessUrl;

    @Inject
    @ConfigProperty(name = "OPENCAT_BUSINESS_TIMING_LOG_LEVEL", defaultValue = "INFO")
    private OpencatBusinessConnector.TimingLogLevel level;

    OpencatBusinessConnector opencatBusinessConnector;

    @PostConstruct
    public void initializeConnector() {
        opencatBusinessConnector = OpencatBusinessConnectorFactory.create(opencatBusinessUrl, level);
    }

    @Produces
    public OpencatBusinessConnector getInstance() {
        return opencatBusinessConnector;
    }

    @PreDestroy
    public void tearDownConnector() {
        opencatBusinessConnector.close();
    }
}
