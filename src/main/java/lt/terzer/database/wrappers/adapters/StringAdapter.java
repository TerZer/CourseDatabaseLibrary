package lt.terzer.database.wrappers.adapters;

import lt.terzer.database.wrappers.Serializer;

public class StringAdapter implements Serializer<String> {
    @Override
    public String dataType() {
        return "TEXT";
    }

    @Override
    public String serialize(String p0) {
        return "'"+p0+"'";
    }

    @Override
    public String deserialize(String p0) {
        return p0;
    }
}
