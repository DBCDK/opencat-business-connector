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
        OpencatBusinessConnectorIT.connector = new OpencatBusinessConnector(
                OpencatBusinessConnectorIT.CLIENT, "http://localhost:8081");
        final OpencatBusinessConnectorIT opencatBusinessConnectorTest = new OpencatBusinessConnectorIT();

        validateRecordTests(opencatBusinessConnectorTest);
        checkTemplateTests(opencatBusinessConnectorTest);
        checkTemplateBuildTests(opencatBusinessConnectorTest);
        doubleRecordFrontendTests(opencatBusinessConnectorTest);
        checkDoubleRecord(opencatBusinessConnectorTest);
        doRecategorizationThingsTests(opencatBusinessConnectorTest);
        recategorizationNoteFieldFactoryTests(opencatBusinessConnectorTest);
        buildRecordTests(opencatBusinessConnectorTest);
        getValidateSchemasTests(opencatBusinessConnectorTest);
        sortRecordTests(opencatBusinessConnectorTest);
        preprocessTests(opencatBusinessConnectorTest);
        metacompassTests(opencatBusinessConnectorTest);
    }

    private static void validateRecordTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testCheckThatValidationErrorsIsProperlyReturned();
        connectorTest.testSanityCheckValidateRecordJSMethod();
    }

    private static void checkTemplateTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testCheckTemplateTestFBSTrue();
        connectorTest.testCheckTemplateTestFBSFalse();
    }

    private static void checkTemplateBuildTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testCheckTemplateBuild_true();
        connectorTest.testCheckTemplateBuild_false();
    }

    private static void doubleRecordFrontendTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testCheckDoubleRecordFrontend_ok();
        connectorTest.testCheckDoubleRecordFrontend_fail();
    }

    private static void checkDoubleRecord(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testCheckDoubleRecord();
    }

    private static void recategorizationNoteFieldFactoryTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testRecategorizationNoteFieldFactory();
    }

    private static void doRecategorizationThingsTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testDoRecatogorizationThings();
    }

    private static void buildRecordTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testBuildRecordWithRecord();
        connectorTest.testBuildRecordWithoutRecord();
    }

    private static void getValidateSchemasTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testGetValidateSchemas_dbc();
    }

    private static void sortRecordTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testSortRecord();
    }

    private static void preprocessTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testPreprocess();
    }

    private static void metacompassTests(OpencatBusinessConnectorIT connectorTest) throws Exception {
        connectorTest.testMetacompass();
        connectorTest.testMetacompass_ErrorCheck();
    }
}
