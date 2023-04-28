package dk.dbc.opencat.connector;

import dk.dbc.common.records.RecordContentTransformer;
import dk.dbc.dataio.commons.utils.lang.StringUtil;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.jsonb.JSONBContext;
import dk.dbc.jsonb.JSONBException;
import dk.dbc.marc.binding.DataField;
import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.reader.MarcReaderException;
import dk.dbc.opencatbusiness.dto.BuildRecordRequestDTO;
import dk.dbc.opencatbusiness.dto.CheckTemplateBuildRequestDTO;
import dk.dbc.opencatbusiness.dto.CheckTemplateBuildResponseDTO;
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
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * OpencatBusinessConnector - opencat-business service client
 * <p>
 * To use this class, you construct an instance, specifying a web resources client as well as
 * a base URL for the opencat-business service endpoint you will be communicating with.
 * </p>
 * <p>
 * This class is thread safe, as long as the given web resources client remains thread safe.
 * </p>
 * <p>
 * Service home: <a href="https://github.com/DBCDK/opencat-business">https://github.com/DBCDK/opencat-business</a>
 * </p>
 */
public class OpencatBusinessConnector {
    JSONBContext jsonbContext = new JSONBContext();

    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OpencatBusinessConnector.class);
    private static final String PATH_VALIDATE_RECORD = "/api/v1/validateRecord";
    private static final String PATH_CHECK_TEMPLATE = "/api/v1/checkTemplate";
    private static final String PATH_CHECK_TEMPLATE_BUILD = "/api/v1/checkTemplateBuild";
    private static final String PATH_CHECK_DOUBLE_RECORD_FRONTEND = "/api/v1/checkDoubleRecordFrontend";
    private static final String PATH_CHECK_DOUBLE_RECORD = "/api/v1/checkDoubleRecord";
    private static final String PATH_DO_RECATEGORIZATION_THINGS = "/api/v1/doRecategorizationThings";
    private static final String PATH_RECATEGORIZATION_NOTE_FIELD_FACTORY = "/api/v1/recategorizationNoteFieldFactory";
    private static final String PATH_BUILD_RECORD = "/api/v1/buildRecord";
    private static final String PATH_SORT_RECORD = "/api/v1/sortRecord";
    private static final String PATH_GET_VALIDATE_SCHEMAS = "/api/v1/getValidateSchemas";
    private static final String PATH_PRE_PROCESS = "/api/v1/preprocess";
    private static final String PATH_META_COMPASS = "/api/v1/metacompass";

    private static final RetryPolicy<Response> RETRY_POLICY = new RetryPolicy<Response>()
            .handle(ProcessingException.class)
            .handleResultIf(response -> response.getStatus() == 404)
            .withDelay(Duration.ofSeconds(10))
            .withMaxRetries(6);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for opencatbusiness service endpoint
     */
    public OpencatBusinessConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for opencatbusiness service endpoint
     * @param level      timings log level
     */
    public OpencatBusinessConnector(Client httpClient, String baseUrl, TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for opencatbusiness service endpoint
     */
    public OpencatBusinessConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for opencatbusiness service endpoint
     * @param level              timings log level
     */
    public OpencatBusinessConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, TimingLogLevel level) {
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

    public void close() {
        failSafeHttpClient.getClient().close();
    }

    public List<MessageEntryDTO> validateRecord(String schemaName, MarcRecord marcRecord) throws OpencatBusinessConnectorException, JSONBException {
        return validateRecord(schemaName, marcRecord, null);
    }

    public List<MessageEntryDTO> validateRecord(String schemaName, MarcRecord marcRecord, String trackingId) throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final ValidateRecordRequestDTO requestDTO = new ValidateRecordRequestDTO();
            requestDTO.setTemplateName(schemaName);
            requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

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
        return checkTemplate(name, groupId, libraryType, null);
    }

    public boolean checkTemplate(String name,
                                 String groupId,
                                 String libraryType,
                                 String trackingId) throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final CheckTemplateRequestDTO requestDTO = new CheckTemplateRequestDTO();
            requestDTO.setName(name);
            requestDTO.setGroupId(groupId);
            requestDTO.setLibraryType(libraryType);
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            return sendPostRequestWithReturn(PATH_CHECK_TEMPLATE, requestDTO, Boolean.class);
        } finally {
            logger.log("checkTemplate took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public boolean checkTemplateBuild(String name) throws OpencatBusinessConnectorException, JSONBException {
        return checkTemplateBuild(name, null);
    }

    public boolean checkTemplateBuild(String name, String trackingId) throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final CheckTemplateBuildRequestDTO requestDTO = new CheckTemplateBuildRequestDTO();
            requestDTO.setName(name);
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            final CheckTemplateBuildResponseDTO responseDTO = sendPostRequestWithReturn(PATH_CHECK_TEMPLATE_BUILD, requestDTO, CheckTemplateBuildResponseDTO.class);

            return responseDTO.isResult();
        } finally {
            logger.log("checkTemplateBuild took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public DoubleRecordFrontendStatusDTO checkDoubleRecordFrontend(MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException {
        return checkDoubleRecordFrontend(marcRecord, null);
    }

    public DoubleRecordFrontendStatusDTO checkDoubleRecordFrontend(MarcRecord marcRecord, String trackingId)
            throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            return sendPostRequestWithReturn(PATH_CHECK_DOUBLE_RECORD_FRONTEND, requestDTO, DoubleRecordFrontendStatusDTO.class);
        } finally {
            logger.log("checkDoubleRecordFrontend took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public void checkDoubleRecord(MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException {
        checkDoubleRecord(marcRecord, null);
    }

    public void checkDoubleRecord(MarcRecord marcRecord, String trackingId)
            throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            sendPostRequestWithoutReturn(PATH_CHECK_DOUBLE_RECORD, requestDTO);
        } finally {
            logger.log("checkDoubleRecordFrontend took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord doRecategorizationThings(MarcRecord currentRecord,
                                               MarcRecord updateRecord,
                                               MarcRecord newRecord)
            throws OpencatBusinessConnectorException, JSONBException, MarcReaderException {
        return doRecategorizationThings(currentRecord, updateRecord, newRecord, null);
    }

    public MarcRecord doRecategorizationThings(MarcRecord currentRecord,
                                               MarcRecord updateRecord,
                                               MarcRecord newRecord,
                                               String trackingId)
            throws OpencatBusinessConnectorException, JSONBException, MarcReaderException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final DoRecategorizationThingsRequestDTO requestDTO = new DoRecategorizationThingsRequestDTO();
            requestDTO.setCurrentRecord(marcRecordToDTOString(currentRecord));
            requestDTO.setUpdateRecord(marcRecordToDTOString(updateRecord));
            requestDTO.setNewRecord(marcRecordToDTOString(newRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            final RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_DO_RECATEGORIZATION_THINGS, requestDTO, RecordResponseDTO.class);

            return RecordContentTransformer.decodeRecord(recordResponseDTO.getRecord().getBytes());
        } finally {
            logger.log("doRecategorizationThings took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public DataField recategorizationNoteFieldFactory(MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException {
        return recategorizationNoteFieldFactory(marcRecord, null);
    }

    public DataField recategorizationNoteFieldFactory(MarcRecord marcRecord, String trackingId)
            throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            return sendPostRequestWithReturn(PATH_RECATEGORIZATION_NOTE_FIELD_FACTORY, requestDTO, FakeDataField.class).toDataField();
        } finally {
            logger.log("recategorizationNoteFieldFactory took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord buildRecord(String templateName, MarcRecord marcRecord, String trackingId)
            throws OpencatBusinessConnectorException, JSONBException, MarcReaderException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final BuildRecordRequestDTO requestDTO = new BuildRecordRequestDTO();
            requestDTO.setTemplateName(templateName);
            if (marcRecord != null) {
                requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            }
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            final RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_BUILD_RECORD, requestDTO, RecordResponseDTO.class);

            return RecordContentTransformer.decodeRecord(recordResponseDTO.getRecord().getBytes());
        } finally {
            logger.log("buildRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord sortRecord(String templateProvider, MarcRecord marcRecord)
            throws OpencatBusinessConnectorException, JSONBException, MarcReaderException {
        return sortRecord(templateProvider, marcRecord, null);
    }

    public MarcRecord sortRecord(String templateProvider, MarcRecord marcRecord, String trackingId)
            throws OpencatBusinessConnectorException, JSONBException, MarcReaderException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final SortRecordRequestDTO requestDTO = new SortRecordRequestDTO();
            requestDTO.setTemplateProvider(templateProvider);
            requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            final RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_SORT_RECORD, requestDTO, RecordResponseDTO.class);

            return RecordContentTransformer.decodeRecord(recordResponseDTO.getRecord().getBytes());
        } finally {
            logger.log("sortRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public List<SchemaDTO> getValidateSchemas(String templateGroup, Set<String> allowedLibraryRules)
            throws OpencatBusinessConnectorException, JSONBException {
        return getValidateSchemas(templateGroup, allowedLibraryRules, null);
    }

    public List<SchemaDTO> getValidateSchemas(String templateGroup, Set<String> allowedLibraryRules, String trackingId)
            throws OpencatBusinessConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final GetValidateSchemasRequestDTO requestDTO = new GetValidateSchemasRequestDTO();
            requestDTO.setTemplateGroup(templateGroup);
            requestDTO.setAllowedLibraryRules(allowedLibraryRules);
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }
            final InputStream responseStream = sendPostRequestWithReturn(PATH_GET_VALIDATE_SCHEMAS, requestDTO, InputStream.class);

            return Arrays.asList(jsonbContext.unmarshall(StringUtil.asString(responseStream), SchemaDTO[].class));
        } finally {
            logger.log("getValidateSchemas took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord preprocess(MarcRecord marcRecord)
            throws JSONBException, OpencatBusinessConnectorException, MarcReaderException {
        return preprocess(marcRecord, null);
    }

    public MarcRecord preprocess(MarcRecord marcRecord, String trackingId)
            throws JSONBException, OpencatBusinessConnectorException, MarcReaderException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            final RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_PRE_PROCESS, requestDTO, RecordResponseDTO.class);

            return RecordContentTransformer.decodeRecord(recordResponseDTO.getRecord().getBytes());
        } finally {
            logger.log("preprocess took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public MarcRecord metacompass(MarcRecord marcRecord)
            throws JSONBException, OpencatBusinessConnectorException, MarcReaderException {
        return metacompass(marcRecord, null);
    }

    public MarcRecord metacompass(MarcRecord marcRecord, String trackingId)
            throws JSONBException, OpencatBusinessConnectorException, MarcReaderException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final RecordRequestDTO requestDTO = new RecordRequestDTO();
            requestDTO.setRecord(marcRecordToDTOString(marcRecord));
            if (trackingId != null) {
                requestDTO.setTrackingId(trackingId);
            }

            final RecordResponseDTO recordResponseDTO = sendPostRequestWithReturn(PATH_META_COMPASS, requestDTO, RecordResponseDTO.class);

            return RecordContentTransformer.decodeRecord(recordResponseDTO.getRecord().getBytes());
        } finally {
            logger.log("metacompass took {} milliseconds",
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
            if (actualStatus == Response.Status.INTERNAL_SERVER_ERROR) {
                // Error message in Danish as this will properly be shown to a user
                throw new OpencatBusinessConnectorException("Det skete en uventet fejl i opencat-business");
            } else {
                // In case the error is not internal server error the exception will probably contain a validation
                // message which should be shown to the user
                throw new OpencatBusinessConnectorException(response.readEntity(String.class));
            }
        }
    }

    private String marcRecordToDTOString(MarcRecord marcRecord) {
        if (marcRecord == null) {
            return null;
        } else {
            byte[] recordBytes = RecordContentTransformer.encodeRecord(marcRecord);
            if (recordBytes == null) {
                return null;
            } else {
                return new String(recordBytes);
            }
        }
    }

    @FunctionalInterface
    interface LogLevelMethod {
        void log(String format, Object... objs);
    }
}
