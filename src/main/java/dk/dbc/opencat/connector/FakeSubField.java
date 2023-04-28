package dk.dbc.opencat.connector;

/*
    This class is used for translating the json send from the javascript to SubField object.
    The class can't be serialized directly because the properties have slightly different names.
 */
public class FakeSubField {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
