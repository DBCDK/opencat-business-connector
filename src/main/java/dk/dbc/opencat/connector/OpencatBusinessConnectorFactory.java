/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.opencat.connector;

import dk.dbc.httpclient.HttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.client.Client;

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
