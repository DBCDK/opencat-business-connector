package dk.dbc.opencat.connector;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.common.records.utils.RecordContentTransformer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.updateservice.dto.DoubleRecordFrontendDTO;
import dk.dbc.updateservice.dto.DoubleRecordFrontendStatusDTO;
import dk.dbc.updateservice.dto.MessageEntryDTO;
import dk.dbc.updateservice.dto.SchemaDTO;
import dk.dbc.updateservice.dto.TypeEnumDTO;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OpencatBusinessConnectorTest {
    private static WireMockServer wireMockServer;
    private static String wireMockHost;
    static OpencatBusinessConnector connector;

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

    @Test
    void sanityCheckValidateRecordJSMethod() throws Exception {
        final String marcString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record xmlns=\"info:lc/xmlns/marcxchange-v1\" xsi:schemaLocation=\"http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <leader>00000n    2200000   4500</leader>\n" +
                "    <datafield tag=\"001\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">68693268</subfield>\n" +
                "        <subfield code=\"b\">870979</subfield>\n" +
                "        <subfield code=\"c\">20181108150323</subfield>\n" +
                "        <subfield code=\"d\">20131129</subfield>\n" +
                "        <subfield code=\"f\">a</subfield>\n" +
                "        <subfield code=\"t\">faust</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"004\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"r\">n</subfield>\n" +
                "        <subfield code=\"a\">e</subfield>\n" +
                "        <subfield code=\"x\">n</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"008\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"t\">h</subfield>\n" +
                "        <subfield code=\"v\">9</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"025\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">5237167</subfield>\n" +
                "        <subfield code=\"2\">viaf</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"025\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">0000000013134949</subfield>\n" +
                "        <subfield code=\"2\">isni</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"040\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">DBC</subfield>\n" +
                "        <subfield code=\"b\">dan</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"043\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"c\">dk</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"100\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">Meilby</subfield>\n" +
                "        <subfield code=\"h\">Mogens</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"375\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">1</subfield>\n" +
                "        <subfield code=\"2\">iso5218</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "</record>\n";

        final MarcRecord marcRecord = RecordContentTransformer.decodeRecord(marcString.getBytes());
        final List<MessageEntryDTO> actualRespons = connector.validateRecord("dbcautoritet", marcRecord);
        assertThat("OpencatBusiness returns empty list", actualRespons.size(), is(0));
    }

    @Test
    void checkThatValidationErrorsIsProperlyReturned() throws Exception {
        String marcString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record xmlns=\"info:lc/xmlns/marcxchange-v1\" xsi:schemaLocation=\"http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <leader>00000n    2200000   4500</leader>\n" +
                "    <datafield tag=\"001\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">68693268</subfield>\n" +
                "        <subfield code=\"b\">870970</subfield>\n" +
                "        <subfield code=\"c\">20181108150323</subfield>\n" +
                "        <subfield code=\"d\">20131129</subfield>\n" +
                "        <subfield code=\"f\">a</subfield>\n" +
                "        <subfield code=\"t\">faust</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"004\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"r\">n</subfield>\n" +
                "        <subfield code=\"a\">e</subfield>\n" +
                "        <subfield code=\"x\">n</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"008\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"t\">h</subfield>\n" +
                "        <subfield code=\"v\">9</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"025\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">5237167</subfield>\n" +
                "        <subfield code=\"2\">viaf</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"025\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">0000000013134949</subfield>\n" +
                "        <subfield code=\"2\">isni</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"040\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">DBC</subfield>\n" +
                "        <subfield code=\"b\">dan</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"043\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"c\">dk</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"100\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">Meilby</subfield>\n" +
                "        <subfield code=\"h\">Mogens</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield tag=\"375\" ind1=\"0\" ind2=\"0\">\n" +
                "        <subfield code=\"a\">1</subfield>\n" +
                "        <subfield code=\"2\">iso5218</subfield>\n" +
                "        <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "    </datafield>\n" +
                "</record>\n";

        final MarcRecord marcRecord = RecordContentTransformer.decodeRecord(marcString.getBytes());

        final MessageEntryDTO expected = new MessageEntryDTO();
        expected.setType(TypeEnumDTO.ERROR);
        expected.setCode(null);
        expected.setUrlForDocumentation("http://www.kat-format.dk/danMARC2/bilag_h/felt001.htm");
        expected.setOrdinalPositionOfField(0);
        expected.setOrdinalPositionOfSubfield(1);
        expected.setOrdinalPositionInSubfield(null);
        expected.setMessage("Værdien '870970' i felt '001' delfelt 'b' er ikke en del af de valide værdier: '870979'");

        final List<MessageEntryDTO> actualResponse = connector.validateRecord("dbcautoritet", marcRecord);
        assertThat("OpencatBusiness returns list with one validation error", actualResponse.size(), is(1));
        assertThat("The validation message is the expected one", actualResponse.get(0), is(expected));
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

    //@Test
    void checkTemplateBuild_true() throws Exception {
        final boolean actual = connector.checkTemplateBuild("allowall");

        assertThat("checkTemplateBuild returns true for allowall", actual, is(true));
    }

    //@Test
    void checkTemplateBuild_false() throws Exception {
        final boolean actual = connector.checkTemplateBuild("julemand");

        assertThat("checkTemplateBuild returns false for julemand", actual, is(false));
    }

    @Test
    void checkDoubleRecordFrontend_ok() throws Exception {
        final String marcString = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n" +
                "<record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:lc/xmlns/marcxchange-v1 http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\">\n" +
                "    <leader>00000n    2200000   4500</leader>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">\n" +
                "        <subfield code=\"a\">50938409</subfield>\n" +
                "        <subfield code=\"b\">870970</subfield>\n" +
                "        <subfield code=\"c\">20191218013539</subfield>\n" +
                "        <subfield code=\"d\">20140131</subfield>\n" +
                "        <subfield code=\"f\">a</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"004\">\n" +
                "        <subfield code=\"r\">n</subfield>\n" +
                "        <subfield code=\"a\">e</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"008\">\n" +
                "        <subfield code=\"t\">m</subfield>\n" +
                "        <subfield code=\"u\">f</subfield>\n" +
                "        <subfield code=\"a\">2014</subfield>\n" +
                "        <subfield code=\"b\">dk</subfield>\n" +
                "        <subfield code=\"d\">2</subfield>\n" +
                "        <subfield code=\"d\">å</subfield>\n" +
                "        <subfield code=\"d\">x</subfield>\n" +
                "        <subfield code=\"l\">dan</subfield>\n" +
                "        <subfield code=\"o\">b</subfield>\n" +
                "        <subfield code=\"v\">0</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"009\">\n" +
                "        <subfield code=\"a\">a</subfield>\n" +
                "        <subfield code=\"g\">xx</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">\n" +
                "        <subfield code=\"c\">ib.</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"032\">\n" +
                "        <subfield code=\"a\">DBF201409</subfield>\n" +
                "        <subfield code=\"x\">BKM201409</subfield>\n" +
                "        <subfield code=\"x\">ACC201405</subfield>\n" +
                "        <subfield code=\"x\">DAT991605</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"038\">\n" +
                "        <subfield code=\"a\">bi</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"041\">\n" +
                "        <subfield code=\"a\">dan</subfield>\n" +
                "        <subfield code=\"c\">nor</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"100\">\n" +
                "        <subfield code=\"5\">870979</subfield>\n" +
                "        <subfield code=\"6\">69208045</subfield>\n" +
                "        <subfield code=\"4\">aut</subfield>\n" +
                "        <subfield code=\"4\">art</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"241\">\n" +
                "        <subfield code=\"a\">Odd er et egg</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
                "        <subfield code=\"a\">Ib er et æggehoved</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"250\">\n" +
                "        <subfield code=\"a\">1. udgave</subfield>\n" +
                "        <subfield code=\"b\">÷</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"260\">\n" +
                "        <subfield code=\"&amp;\">1</subfield>\n" +
                "        <subfield code=\"a\">Hedehusene</subfield>\n" +
                "        <subfield code=\"b\">Torgard</subfield>\n" +
                "        <subfield code=\"c\">2014</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"300\">\n" +
                "        <subfield code=\"a\">[36] sider</subfield>\n" +
                "        <subfield code=\"b\">alle ill. i farver</subfield>\n" +
                "        <subfield code=\"c\">28 cm</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"504\">\n" +
                "        <subfield code=\"&amp;\">1</subfield>\n" +
                "        <subfield code=\"a\">Billedbog. Hver morgen pakker Ib sit hoved ind i håndklæder og en tehætte. Hans hoved er nemlig et æg, og han skal hele tiden passe på, at det ikke går i stykker. Men så møder han Sif. Hun passer ikke på noget</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"521\">\n" +
                "        <subfield code=\"&amp;\">REX</subfield>\n" +
                "        <subfield code=\"b\">1. oplag</subfield>\n" +
                "        <subfield code=\"c\">2014</subfield>\n" +
                "        <subfield code=\"k\">Arcorounborg</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "        <subfield code=\"n\">85</subfield>\n" +
                "        <subfield code=\"z\">296</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "        <subfield code=\"o\">sk</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"s\">alene</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"s\">ensomhed</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"s\">venskab</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"s\">kærlighed</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"s\">tapperhed</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"s\">mod</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"u\">for 4 år</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"u\">for 5 år</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"u\">for 6 år</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"u\">for 7 år</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"720\">\n" +
                "        <subfield code=\"o\">Hugin Eide</subfield>\n" +
                "        <subfield code=\"4\">trl</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"990\">\n" +
                "        <subfield code=\"o\">201409</subfield>\n" +
                "        <subfield code=\"b\">l</subfield>\n" +
                "        <subfield code=\"b\">b</subfield>\n" +
                "        <subfield code=\"b\">s</subfield>\n" +
                "        <subfield code=\"u\">nt</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"996\">\n" +
                "        <subfield code=\"a\">DBC</subfield>\n" +
                "    </datafield>\n" +
                "</record>\n";

        final MarcRecord marcRecord = RecordContentTransformer.decodeRecord(marcString.getBytes());
        final DoubleRecordFrontendStatusDTO actual = connector.checkDoubleRecordFrontend(marcRecord);

        assertThat("Check status for checkDoubleRecordFrontend", actual.getStatus(), is("ok"));
    }

    @Test
    void checkDoubleRecordFrontend_fail() throws Exception {
        final String marcString = "<record xmlns=\"info:lc/xmlns/marcxchange-v1\">\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">\n" +
                "        <subfield code=\"a\">52958858</subfield>\n" +
                "        <subfield code=\"b\">870970</subfield>\n" +
                "        <subfield code=\"c\">20170616143600</subfield>\n" +
                "        <subfield code=\"d\">20180628</subfield>\n" +
                "        <subfield code=\"f\">a</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">\n" +
                "        <subfield code=\"e\">9782843090387</subfield>\n" +
                "    </datafield>\n" +
                "</record>";

        final MarcRecord marcRecord = RecordContentTransformer.decodeRecord(marcString.getBytes());

        final DoubleRecordFrontendDTO expected = new DoubleRecordFrontendDTO();
        expected.setMessage("Double record for record 52958858, reason: 021e");
        expected.setPid("52958857:870970");

        final DoubleRecordFrontendStatusDTO expectedStatus = new DoubleRecordFrontendStatusDTO();
        expectedStatus.setStatus("doublerecord");
        expectedStatus.setDoubleRecordFrontendDTOs(Collections.singletonList(expected));

        final DoubleRecordFrontendStatusDTO actual = connector.checkDoubleRecordFrontend(marcRecord);

        assertThat("Check status for checkDoubleRecordFrontend", actual, is(expectedStatus));
    }

    @Test
    void getValidateSchemas_dbc() throws Exception {
        final List<SchemaDTO> actual = connector.getValidateSchemas("dbc", new HashSet<>());

        final List<SchemaDTO> expected = new ArrayList<>();

        final SchemaDTO allowAll = new SchemaDTO();
        allowAll.setSchemaName("allowall");
        allowAll.setSchemaInfo("");
        expected.add(allowAll);

        final SchemaDTO BCIbog = new SchemaDTO();
        BCIbog.setSchemaName("BCIbog");
        BCIbog.setSchemaInfo("Skabelon til katalogisering af fysiske bøger - enkeltstående post.");
        expected.add(BCIbog);

        final SchemaDTO BCIbogbind = new SchemaDTO();
        BCIbogbind.setSchemaName("BCIbogbind");
        BCIbogbind.setSchemaInfo("Skabelon til katalogisering af flerbindsværk af fysiske bøger - bindpost.");
        expected.add(BCIbogbind);

        final SchemaDTO BCIboghoved = new SchemaDTO();
        BCIboghoved.setSchemaName("BCIboghoved");
        BCIboghoved.setSchemaInfo("Skabelon til katalogisering af flerbindsværk af fysiske bøger - hovedpost.");
        expected.add(BCIboghoved);

        final SchemaDTO dbclittolk = new SchemaDTO();
        dbclittolk.setSchemaName("dbclittolk");
        dbclittolk.setSchemaInfo("");
        expected.add(dbclittolk);

        assertThat("List of schemas", actual, is(expected));
    }

    @Test
    void sortRecord() throws Exception {
        final String marcString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:lc/xmlns/marcxchange-v1 http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\">\n" +
                "    <leader>00000     22000000 4500 </leader>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">\n" +
                "        <subfield code=\"a\">43645676</subfield>\n" +
                "        <subfield code=\"b\">870970</subfield>\n" +
                "        <subfield code=\"c\">20070726122101</subfield>\n" +
                "        <subfield code=\"d\">20070726</subfield>\n" +
                "        <subfield code=\"f\">a</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"002\">\n" +
                "        <subfield code=\"b\">721700</subfield>\n" +
                "        <subfield code=\"c\">95487653</subfield>\n" +
                "        <subfield code=\"x\">72170095487653</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"004\">\n" +
                "        <subfield code=\"r\">c</subfield>\n" +
                "        <subfield code=\"a\">e</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"008\">\n" +
                "        <subfield code=\"v\">0</subfield>\n" +
                "        <subfield code=\"a\">2003</subfield>\n" +
                "        <subfield code=\"b\">us</subfield>\n" +
                "        <subfield code=\"l\">eng</subfield>\n" +
                "        <subfield code=\"t\">m</subfield>\n" +
                "        <subfield code=\"u\">f</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"009\">\n" +
                "        <subfield code=\"a\">a</subfield>\n" +
                "        <subfield code=\"g\">xx</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">\n" +
                "        <subfield code=\"a\">1-56971-998-5</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"100\">\n" +
                "        <subfield code=\"a\">Powell</subfield>\n" +
                "        <subfield code=\"h\">Eric</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
                "        <subfield code=\"a\">The ¤Goon, nothin' but misery</subfield>\n" +
                "        <subfield code=\"e\">by Eric Powell</subfield>\n" +
                "        <subfield code=\"f\">colors by Eric and Robin Powell</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"260\">\n" +
                "        <subfield code=\"a\">Milwaukie</subfield>\n" +
                "        <subfield code=\"b\">Dark Horse</subfield>\n" +
                "        <subfield code=\"c\">2003</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"300\">\n" +
                "        <subfield code=\"a\">1 bind</subfield>\n" +
                "        <subfield code=\"b\">ill. i farver</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"440\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"a\">The ¤Goon</subfield>\n" +
                "        <subfield code=\"V\">1</subfield>\n" +
                "        <subfield code=\"v\">Volume 1</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"505\">\n" +
                "        <subfield code=\"a\">Tegneserie</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"520\">\n" +
                "        <subfield code=\"a\">Tidligere udgivet som enkelthæfter</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "        <subfield code=\"m\">83.8</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"745\">\n" +
                "        <subfield code=\"a\">Nothing but misery</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"996\">\n" +
                "        <subfield code=\"a\">710100</subfield>\n" +
                "    </datafield>\n" +
                "</record>\n";

        final String marcStringExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:lc/xmlns/marcxchange-v1 http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\">\n" +
                "    <leader>00000     22000000 4500 </leader>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">\n" +
                "        <subfield code=\"a\">43645676</subfield>\n" +
                "        <subfield code=\"b\">870970</subfield>\n" +
                "        <subfield code=\"c\">20070726122101</subfield>\n" +
                "        <subfield code=\"d\">20070726</subfield>\n" +
                "        <subfield code=\"f\">a</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"002\">\n" +
                "        <subfield code=\"b\">721700</subfield>\n" +
                "        <subfield code=\"c\">95487653</subfield>\n" +
                "        <subfield code=\"x\">72170095487653</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"004\">\n" +
                "        <subfield code=\"r\">c</subfield>\n" +
                "        <subfield code=\"a\">e</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"008\">\n" +
                "        <subfield code=\"t\">m</subfield>\n" +
                "        <subfield code=\"u\">f</subfield>\n" +
                "        <subfield code=\"a\">2003</subfield>\n" +
                "        <subfield code=\"b\">us</subfield>\n" +
                "        <subfield code=\"l\">eng</subfield>\n" +
                "        <subfield code=\"v\">0</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"009\">\n" +
                "        <subfield code=\"a\">a</subfield>\n" +
                "        <subfield code=\"g\">xx</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">\n" +
                "        <subfield code=\"a\">1-56971-998-5</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"100\">\n" +
                "        <subfield code=\"a\">Powell</subfield>\n" +
                "        <subfield code=\"h\">Eric</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
                "        <subfield code=\"a\">The ¤Goon, nothin' but misery</subfield>\n" +
                "        <subfield code=\"e\">by Eric Powell</subfield>\n" +
                "        <subfield code=\"f\">colors by Eric and Robin Powell</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"260\">\n" +
                "        <subfield code=\"a\">Milwaukie</subfield>\n" +
                "        <subfield code=\"b\">Dark Horse</subfield>\n" +
                "        <subfield code=\"c\">2003</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"300\">\n" +
                "        <subfield code=\"a\">1 bind</subfield>\n" +
                "        <subfield code=\"b\">ill. i farver</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"440\">\n" +
                "        <subfield code=\"0\"/>\n" +
                "        <subfield code=\"a\">The ¤Goon</subfield>\n" +
                "        <subfield code=\"V\">1</subfield>\n" +
                "        <subfield code=\"v\">Volume 1</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"505\">\n" +
                "        <subfield code=\"a\">Tegneserie</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"520\">\n" +
                "        <subfield code=\"a\">Tidligere udgivet som enkelthæfter</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "        <subfield code=\"m\">83.8</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"745\">\n" +
                "        <subfield code=\"a\">Nothing but misery</subfield>\n" +
                "    </datafield>\n" +
                "    <datafield ind1=\"0\" ind2=\"0\" tag=\"996\">\n" +
                "        <subfield code=\"a\">710100</subfield>\n" +
                "    </datafield>\n" +
                "</record>\n";

        final MarcRecord marcRecord = RecordContentTransformer.decodeRecord(marcString.getBytes());
        final MarcRecord expected = RecordContentTransformer.decodeRecord(marcStringExpected.getBytes());

        final MarcRecord actual = connector.sortRecord("bogbind", marcRecord);

        assertThat(actual, is(expected));
    }

}
