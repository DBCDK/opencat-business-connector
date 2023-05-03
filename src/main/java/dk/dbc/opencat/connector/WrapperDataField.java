package dk.dbc.opencat.connector;

import dk.dbc.marc.binding.DataField;
import dk.dbc.marc.binding.SubField;

import java.util.List;

/*
    This class is used for translating the json send from the javascript to DataField object.
    The class can't be serialized directly because the properties have slightly different names.
 */
public class WrapperDataField {

    private String name;
    private String indicator;
    private List<WrapperSubField> subfields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicators) {
        this.indicator = indicators;
    }

    public List<WrapperSubField> getSubfields() {
        return subfields;
    }

    public void setSubfields(List<WrapperSubField> subfields) {
        this.subfields = subfields;
    }

    public DataField toDataField() {
        final DataField dataField = new DataField(name, indicator);
        for (WrapperSubField wrapperSubField : subfields) {
            dataField.addSubField(new SubField(wrapperSubField.getName().charAt(0), wrapperSubField.getValue()));
        }

        return dataField;
    }

}
