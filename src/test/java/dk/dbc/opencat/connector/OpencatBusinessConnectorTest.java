package dk.dbc.opencat.connector;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.dataio.jsonb.JSONBContext;
import dk.dbc.dataio.jsonb.JSONBException;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.updateservice.dto.MessageEntryDTO;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OpencatBusinessConnectorTest {
    private static WireMockServer wireMockServer;
    private static String wireMockHost;
    static OpencatBusinessConnector connector;
    private final JSONBContext jsonbContext = new JSONBContext();

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(options().dynamicPort()
                .dynamicHttpsPort());
        wireMockServer.start();
        wireMockHost = "http://localhost:" + wireMockServer.port();
        configureFor("localhost", wireMockServer.port());
    }

    @BeforeAll
    static void setConnector() {
        connector = new OpencatBusinessConnector(CLIENT, wireMockHost, OpencatBusinessConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWiremockServer() {
        wireMockServer.stop();
    }

    //@Test
    void sanityCheckValidateRecordJSMethod() throws JSONBException, dk.dbc.jsonb.JSONBException, OpencatBusinessConnectorException {
        MarcRecord marcRecord = jsonbContext.unmarshall("{\"fields\":[{\"name\":\"001\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"68693268\"},{\"name\":\"b\",\"value\":\"870979\"},{\"name\":\"c\",\"value\":\"20181108150337\"},{\"name\":\"d\",\"value\":\"20131129\"},{\"name\":\"f\",\"value\":\"a\"},{\"name\":\"t\",\"value\":\"faust\"}]},{\"name\":\"004\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"r\",\"value\":\"n\"},{\"name\":\"a\",\"value\":\"e\"},{\"name\":\"x\",\"value\":\"n\"}]},{\"name\":\"008\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"t\",\"value\":\"h\"},{\"name\":\"v\",\"value\":\"9\"}]},{\"name\":\"025\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"5237167\"},{\"name\":\"2\",\"value\":\"viaf\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"025\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"0000000013134949\"},{\"name\":\"2\",\"value\":\"isni\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"040\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"DBC\"},{\"name\":\"b\",\"value\":\"dan\"}]},{\"name\":\"043\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"c\",\"value\":\"dk\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"100\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"Meilby\"},{\"name\":\"h\",\"value\":\"Mogens\"}]},{\"name\":\"375\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"1\"},{\"name\":\"2\",\"value\":\"iso5218\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"d08\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"o\",\"value\":\"autogenereret\"}]},{\"name\":\"xyz\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"u\",\"value\":\"MEILBYMOGENS\"}]},{\"name\":\"z98\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"Minus korrekturprint\"}]},{\"name\":\"z99\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"VIAF\"}]}]}", MarcRecord.class);
        List<MessageEntryDTO> expectedResponse = new ArrayList<>();
        List<MessageEntryDTO> actualRespons = connector.validateRecord("dbcautoritet", marcRecord);
        assertThat("OpencatBusiness returns empty list", actualRespons, is(expectedResponse));
    }

    //@Test
    void checkThatValidationErrorsIsProperlyReturned() throws JSONBException, dk.dbc.jsonb.JSONBException, OpencatBusinessConnectorException {
        MarcRecord marcRecord = jsonbContext.unmarshall("{\"fields\":[{\"name\":\"001\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"68693268\"},{\"name\":\"b\",\"value\":\"870980\"},{\"name\":\"c\",\"value\":\"20181108150337\"},{\"name\":\"d\",\"value\":\"20131129\"},{\"name\":\"f\",\"value\":\"a\"},{\"name\":\"t\",\"value\":\"faust\"}]},{\"name\":\"004\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"r\",\"value\":\"n\"},{\"name\":\"a\",\"value\":\"e\"},{\"name\":\"x\",\"value\":\"n\"}]},{\"name\":\"008\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"t\",\"value\":\"h\"},{\"name\":\"v\",\"value\":\"9\"}]},{\"name\":\"025\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"5237167\"},{\"name\":\"2\",\"value\":\"viaf\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"025\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"0000000013134949\"},{\"name\":\"2\",\"value\":\"isni\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"040\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"DBC\"},{\"name\":\"b\",\"value\":\"dan\"}]},{\"name\":\"043\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"c\",\"value\":\"dk\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"100\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"Meilby\"},{\"name\":\"h\",\"value\":\"Mogens\"}]},{\"name\":\"375\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"1\"},{\"name\":\"2\",\"value\":\"iso5218\"},{\"name\":\"&\",\"value\":\"VIAF\"}]},{\"name\":\"d08\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"o\",\"value\":\"autogenereret\"}]},{\"name\":\"xyz\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"u\",\"value\":\"MEILBYMOGENS\"}]},{\"name\":\"z98\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"Minus korrekturprint\"}]},{\"name\":\"z99\",\"indicator\":\"00\",\"subfields\":[{\"name\":\"a\",\"value\":\"VIAF\"}]}]}", MarcRecord.class);
        MessageEntryDTO[] expectedResponse = jsonbContext.unmarshall("[{\"type\":\"ERROR\",\"urlForDocumentation\":\"http://www.kat-format.dk/danMARC2/bilag_h/felt001.htm\",\"message\":\"Værdien '870980' er ikke en del af de valide værdier: '870979'\",\"ordinalPositionOfSubfield\":1,\"ordinalPositionOfField\":0}]", MessageEntryDTO[].class);
        List<MessageEntryDTO> actualRespons = connector.validateRecord("dbcautoritet", marcRecord);
        assertThat("OpencatBusiness returns list with one validation error", actualRespons, is(Arrays.asList(expectedResponse)));
    }

    @Test
    void checkTemplateTestFBSTrue() throws Exception {
        boolean actual = connector.checkTemplate("netlydbog", "710100", "fbs");

        assertThat("checkTemplate returns true", actual, is(true));
    }

    @Test
    void checkTemplateTestFBSFalse() throws Exception {
        boolean actual = connector.checkTemplate("dbc", "710100", "fbs");

        assertThat("checkTemplate returns false", actual, is(false));
    }
}
