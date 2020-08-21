package dk.dbc.opencat;

import dk.dbc.opencat.connector.OpencatBusinessConnector;

public class OpencatBusinessConnectorTestWireMockRecorder {

        /*
        Steps to reproduce wiremock recording:

        * Start standalone runner
            java -jar wiremock-standalone-{WIRE_MOCK_VERSION}.jar --proxy-all="{OPENCAT_BUSINESS_URL}" --record-mappings --verbose

        * Run the main method of this class

        * Replace content of src/test/resources/{__files|mappings} with that produced by the standalone runner
     */

    public static void main(String[] args) throws Exception {
        OpencatBusinessConnectorTest.connector = new OpencatBusinessConnector(
                OpencatBusinessConnectorTest.CLIENT, "http://localhost:8080");
        final OpencatBusinessConnectorTest opencatBusinessConnectorTest = new OpencatBusinessConnectorTest();

        checkTemplateTests(opencatBusinessConnectorTest);
    }

    private static void checkTemplateTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.checkTemplateTestFBSTrue();
        connectorTest.checkTemplateTestFBSFalse();
    }
}
