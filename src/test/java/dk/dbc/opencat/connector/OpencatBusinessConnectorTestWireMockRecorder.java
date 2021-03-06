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
                OpencatBusinessConnectorTest.CLIENT, "http://localhost:8081");
        final OpencatBusinessConnectorTest opencatBusinessConnectorTest = new OpencatBusinessConnectorTest();

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

    private static void doubleRecordFrontendTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.checkDoubleRecordFrontend_ok();
        connectorTest.checkDoubleRecordFrontend_fail();
    }

    private static void checkDoubleRecord(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.checkDoubleRecord();
    }

    private static void recategorizationNoteFieldFactoryTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.recategorizationNoteFieldFactory();
    }

    private static void doRecategorizationThingsTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.doRecatogorizationThings();
    }

    private static void buildRecordTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.buildRecordWithRecord();
        connectorTest.buildRecordWithoutRecord();
    }

    private static void getValidateSchemasTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.getValidateSchemas_dbc();
    }

    private static void sortRecordTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.sortRecord();
    }

    private static void preprocessTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.preprocess();
    }

    private static void metacompassTests(OpencatBusinessConnectorTest connectorTest) throws Exception {
        connectorTest.metacompass();
        connectorTest.metacompass_ErrorCheck();
    }
}
