package dk.dbc.opencat.connector;

import dk.dbc.common.records.MarcField;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.common.records.utils.RecordContentTransformer;
import dk.dbc.dataio.commons.utils.lang.StringUtil;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.jsonb.JSONBContext;
import dk.dbc.jsonb.JSONBException;
import dk.dbc.opencatbusiness.dto.BuildRecordRequestDTO;
import dk.dbc.opencatbusiness.dto.CheckTemplateRequestDTO;
import dk.dbc.opencatbusiness.dto.DoRecategorizationThingsRequestDTO;
import dk.dbc.opencatbusiness.dto.GetValidateSchemasRequestDTO;
import dk.dbc.opencatbusiness.dto.RecordRequestDTO;
import dk.dbc.opencatbusiness.dto.RecordResponseDTO;
import dk.dbc.opencatbusiness.dto.SortRecordRequestDTO;
import dk.dbc.opencatbusiness.dto.ValidateRecordRequestDTO;
import dk.dbc.updateservice.dto.DoubleRecordFrontendStatusDTO;
import dk.dbc.updateservice.dto.MessageEntryDTO;
import dk.dbc.updateservice.dto.SchemaDTO;
import dk.dbc.util.Stopwatch;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    private static final String PATH_VALIDATE_RECORD = "/api/v1/validateRecord";
    private final static String PATH_CHECK_TEMPLATE = "/api/v1/checkTemplate";
    private final static String PATH_CHECK_TEMPLATE_BUILD = "/api/v1/checkTemplateBuild";
    private final static String PATH_CHECK_DOUBLE_RECORD_FRONTEND = "/api/v1/checkDoubleRecordFrontend";
    private final static String PATH_CHECK_DOUBLE_RECORD = "/api/v1/checkDoubleRecord";
    private final static String PATH_DO_RECATEGORIZATION_THINGS = "/api/v1/doRecategorizationThings";
    private final static String PATH_RECATEGORIZATION_NOTE_FIELD_FACTORY = "/api/v1/recategorizationNoteFieldFactory";
    private final static String PATH_BUILD_RECORD = "/api/v1/buildRecord";
    private final static String PATH_SORT_RECORD = "/api/v1/sortRecord";
    private final static String PATH_GET_VALIDATE_SCHEMAS = "/api/v1/getValidateSchemas";
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

    public List<MessageEntryDTO> validateRecord(String schemaName, MarcRecord marcRecord) throws OpencatBusinessConnectorException, JSONBException, JAXBException, UnsupportedEncodingException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final ValidateRecordRequestDTO requestDTO = new ValidateRecordRequestDTO();
            requestDTO.setTemplateName(schemaName);
            requestDTO.setRecord(new String(RecordContentTransformer.encodeRecord(marcRecord)));
            final InputStream responseStream = sendPostRequestWithReturn(PATH_VALIDATE_RECORD, requestDTO, InputStream.class);

            return Arrays.asList(jsonbContext.unmarshall(StringUtil.asString(responseStream), MessageEntryDTO[].class));
        } finally {
            logger.log("validateRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public boolean checkTemplate(String name,
                                 String groupId,
                                 String libraryType) throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final CheckTemplateRequestDTO requestDTO = new CheckTemplateRequestDTO();
            requestDTO.setName(name);
            requestDTO.setGroupId(groupId);
            requestDTO.setLibraryType(libraryType);

            return sendPostRequestWithReturn(PATH_CHECK_TEMPLATE, requestDTO, Boolean.class);
        } finally {
            logger.log("checkTemplate took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public boolean checkTemplateBuild(String name) throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            return sendPostRequestWithReturn(PATH_CHECK_TEMPLATE_BUILD, name, Boolean.class);
        } finally {
            logger.log("checkTemplateBuild took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public DoubleRecordFrontendStatusDTO checkDoubleRecordFrontend(MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException, JAXBException, UnsupportedEncodingException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(new String(RecordContentTransformer.encodeRecord(marcRecord)));

            return sendPostRequestWithReturn(PATH_CHECK_DOUBLE_RECORD_FRONTEND, requestDTO, DoubleRecordFrontendStatusDTO.class);
        } finally {
            logger.log("checkDoubleRecordFrontend took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public void checkDoubleRecord(MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException, JAXBException, UnsupportedEncodingException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(new String(RecordContentTransformer.encodeRecord(marcRecord)));

            sendPostRequestWithoutReturn(PATH_CHECK_DOUBLE_RECORD, requestDTO);
        } finally {
            logger.log("checkDoubleRecordFrontend took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord doRecategorizationThings(MarcRecord currentRecord,
                                               MarcRecord updateRecord,
                                               MarcRecord newRecord)
            throws OpencatBusinessConnectorException, JSONBException, JAXBException, UnsupportedEncodingException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final DoRecategorizationThingsRequestDTO requestDTO = new DoRecategorizationThingsRequestDTO();
            requestDTO.setCurrentRecord(new String(RecordContentTransformer.encodeRecord(currentRecord)));
            requestDTO.setUpdateRecord(new String(RecordContentTransformer.encodeRecord(updateRecord)));
            requestDTO.setNewRecord(new String(RecordContentTransformer.encodeRecord(newRecord)));

            RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_DO_RECATEGORIZATION_THINGS, requestDTO, RecordResponseDTO.class);

            return jsonbContext.unmarshall(recordResponseDTO.getRecord(), MarcRecord.class);
        } finally {
            logger.log("doRecategorizationThings took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcField recategorizationNoteFieldFactory(MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException, JAXBException, UnsupportedEncodingException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(new String(RecordContentTransformer.encodeRecord(marcRecord)));

            return sendPostRequestWithReturn(PATH_RECATEGORIZATION_NOTE_FIELD_FACTORY, requestDTO, MarcField.class);
        } finally {
            logger.log("recategorizationNoteFieldFactory took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord buildRecord(String templateName)
            throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final BuildRecordRequestDTO requestDTO = new BuildRecordRequestDTO();
            requestDTO.setTemplateName(templateName);

            RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_BUILD_RECORD, requestDTO, RecordResponseDTO.class);

            return jsonbContext.unmarshall(recordResponseDTO.getRecord(), MarcRecord.class);
        } finally {
            logger.log("buildRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord buildRecord(String templateName, MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException, JAXBException, UnsupportedEncodingException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final BuildRecordRequestDTO requestDTO = new BuildRecordRequestDTO();
            requestDTO.setTemplateName(templateName);
            requestDTO.setRecord(new String(RecordContentTransformer.encodeRecord(marcRecord)));

            RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_BUILD_RECORD, requestDTO, RecordResponseDTO.class);

            return jsonbContext.unmarshall(recordResponseDTO.getRecord(), MarcRecord.class);
        } finally {
            logger.log("buildRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord sortRecord(String templateProvider, MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException, JAXBException, UnsupportedEncodingException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final SortRecordRequestDTO requestDTO = new SortRecordRequestDTO();
            requestDTO.setTemplateProvider(templateProvider);
            requestDTO.setRecord(new String(RecordContentTransformer.encodeRecord(marcRecord)));

            RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_SORT_RECORD, requestDTO, RecordResponseDTO.class);

            return RecordContentTransformer.decodeRecord(recordResponseDTO.getRecord().getBytes());
        } finally {
            logger.log("sortRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public List<SchemaDTO> getValidateSchemas(String templateGroup, Set<String> allowedLibraryRules)
            throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final GetValidateSchemasRequestDTO requestDTO = new GetValidateSchemasRequestDTO();
            requestDTO.setTemplateGroup(templateGroup);
            requestDTO.setAllowedLibraryRules(allowedLibraryRules);
            final InputStream responseStream = sendPostRequestWithReturn(PATH_GET_VALIDATE_SCHEMAS, requestDTO, InputStream.class);

            return Arrays.asList(jsonbContext.unmarshall(StringUtil.asString(responseStream), SchemaDTO[].class));
        } finally {
            logger.log("getValidateSchemas took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T sendPostRequestWithReturn(String basePath, Object request, Class<T> type)
            throws OpencatBusinessConnectorException, JSONBException {
        final Response response = sendPostRequest(basePath, request);
        assertResponseStatus(response, Response.Status.OK);
        return readResponseEntity(response, type);
    }

    private void sendPostRequestWithoutReturn(String basePath, Object request)
            throws OpencatBusinessConnectorException, JSONBException {
        final Response response = sendPostRequest(basePath, request);
        assertResponseStatus(response, Response.Status.OK);
    }

    private Response sendPostRequest(String basePath, Object request) throws JSONBException {
        InvariantUtil.checkNotNullOrThrow(request, "request");
        final PathBuilder path = new PathBuilder(basePath);
        final HttpPost post = new HttpPost(failSafeHttpClient)
                .withBaseUrl(baseUrl)
                .withData(jsonbContext.marshall(request), "application/json")
                .withHeader("Accept", "application/json")
                .withPathElements(path.build());

        return post.execute();
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
