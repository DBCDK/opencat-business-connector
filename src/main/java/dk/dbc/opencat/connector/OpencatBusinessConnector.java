package dk.dbc.opencat.connector;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.updateservice.dto.MessageEntryDTO;
import dk.dbc.common.records.MarcRecord;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import dk.dbc.jsonb.JSONBContext;
import dk.dbc.jsonb.JSONBException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import dk.dbc.util.Stopwatch;
import dk.dbc.dataio.commons.utils.lang.StringUtil;

public class OpencatBusinessConnector {
    JSONBContext jsonbContext = new JSONBContext();
    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OpencatBusinessConnector.class);
    private static final RetryPolicy RETRY_POLICY = new RetryPolicy()
            .retryOn(Collections.singletonList(ProcessingException.class))
            .retryIf((Response response) -> response.getStatus() == 404)
            .withDelay(10, TimeUnit.SECONDS)
            .withMaxRetries(1);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final OpencatBusinessConnector.LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for opencatbusiness service endpoint
     */
    public OpencatBusinessConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, OpencatBusinessConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for opencatbusiness service endpoint
     * @param level      timings log level
     */
    public OpencatBusinessConnector(Client httpClient, String baseUrl, OpencatBusinessConnector.TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for opencatbusiness service endpoint
     */
    public OpencatBusinessConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, OpencatBusinessConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for opencatbusiness service endpoint
     * @param level              timings log level
     */
    public OpencatBusinessConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, OpencatBusinessConnector.TimingLogLevel level) {
        this.failSafeHttpClient = InvariantUtil.checkNotNullOrThrow(
                failSafeHttpClient, "failSafeHttpClient");
        this.baseUrl = InvariantUtil.checkNotNullNotEmptyOrThrow(
                baseUrl, "baseUrl");
        switch (level) {
            case TRACE:
                logger = LOGGER::trace;
                break;
            case DEBUG:
                logger = LOGGER::debug;
                break;
            case INFO:
                logger = LOGGER::info;
                break;
            case WARN:
                logger = LOGGER::warn;
                break;
            case ERROR:
                logger = LOGGER::error;
                break;
            default:
                logger = LOGGER::info;
                break;
        }
    }

    public MessageEntryDTO[] validateRecord(String schemaName, MarcRecord marcRecord) throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final InputStream responseStream = sendPostRequest("/api/v1/validateRecord",
                    Arrays.asList(schemaName, jsonbContext.marshall(marcRecord)), InputStream.class);
            return jsonbContext.unmarshall(StringUtil.asString(responseStream), MessageEntryDTO[].class);
        } finally {
            logger.log("validateRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T sendPostRequest(String basePath, Object request, Class<T> type)
            throws OpencatBusinessConnectorException, JSONBException {
        InvariantUtil.checkNotNullOrThrow(request, "request");
        final PathBuilder path = new PathBuilder(basePath);
        final HttpPost post = new HttpPost(failSafeHttpClient)
                .withBaseUrl(baseUrl)
                .withData(jsonbContext.marshall(request), "application/json")
                .withHeader("Accept", "application/json")
                .withPathElements(path.build());

        final Response response = post.execute();
        assertResponseStatus(response, Response.Status.OK);
        return readResponseEntity(response, type);
    }

    private <T> T readResponseEntity(Response response, Class<T> type)
            throws OpencatBusinessConnectorException {
        final T entity = response.readEntity(type);
        if (entity == null) {
            throw new OpencatBusinessConnectorException(
                    String.format("OpencatBusiness returned with null-valued %s entity",
                            type.getName()));
        }
        return entity;
    }

    private void assertResponseStatus(Response response, Response.Status expectedStatus)
            throws OpencatBusinessConnectorException {
        final Response.Status actualStatus =
                Response.Status.fromStatusCode(response.getStatus());
        if (actualStatus != expectedStatus) {
            throw new OpencatBusinessConnectorException(
                    String.format("OpencatBusiness returned with '%s' status code: %s",
                            actualStatus,
                            actualStatus.getStatusCode()));
        }
    }

    public void close() {
        failSafeHttpClient.getClient().close();
    }

    @FunctionalInterface
    interface LogLevelMethod {
        void log(String format, Object... objs);
    }
}
