package lt.terzer.database.wrappers.adapters;

import lt.terzer.database.wrappers.Serializer;

public class DoubleAdapter implements Serializer<Double> {

    @Override
    public String dataType() {
        return "DOUBLE";
    }

    @Override
    public String serialize(Double p0) {
        return "'"+p0+"'";
    }

    @Override
    public Double deserialize(String p0) {
        return Double.parseDouble(p0);
    }
}
