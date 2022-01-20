package lt.terzer.database.wrappers.adapters;

import lt.terzer.database.wrappers.Serializer;

public class BooleanAdapter implements Serializer<Boolean> {
    @Override
    public String dataType() {
        return "BOOLEAN";
    }

    @Override
    public String serialize(Boolean p0) {
        return String.valueOf(p0);
    }

    @Override
    public Boolean deserialize(String p0) {
        return Boolean.parseBoolean(p0);
    }
}
