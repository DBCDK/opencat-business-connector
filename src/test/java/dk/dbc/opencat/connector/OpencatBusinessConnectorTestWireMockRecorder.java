package dk.dbc.opencat.connector;

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

        validateRecordTests(opencatBusinessConnectorTest);
        checkTemplateTests(opencatBusinessConnectorTest);
        checkTemplateBuildTests(opencatBusinessConnectorTest);
        doubleRecordFrontendTest(opencatBusinessConnectorTest);
    }

    private static void validateRecordTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.checkThatValidationErrorsIsProperlyReturned();
        connectorTest.sanityCheckValidateRecordJSMethod();
    }

    private static void checkTemplateTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.checkTemplateTestFBSTrue();
        connectorTest.checkTemplateTestFBSFalse();
    }

    private static void checkTemplateBuildTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.checkTemplateBuild_true();
        connectorTest.checkTemplateBuild_false();
    }

    private static void doubleRecordFrontendTest(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.checkDoubleRecordFrontend_ok();
        connectorTest.checkDoubleRecordFrontend_fail();
    }
}
