package lt.terzer.database.wrappers.adapters;

import lt.terzer.database.wrappers.Serializer;

public class IntegerAdapter implements Serializer<Integer> {
    @Override
    public String dataType() {
        return "INT";
    }

    @Override
    public String serialize(Integer p0) {
        return "'"+p0+"'";
    }

    @Override
    public Integer deserialize(String p0) {
        return Integer.parseInt(p0);
    }
}
