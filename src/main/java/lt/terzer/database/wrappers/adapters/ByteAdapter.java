package lt.terzer.database.wrappers.adapters;

import lt.terzer.database.wrappers.Serializer;

public class ByteAdapter implements Serializer<Byte> {
    @Override
    public String dataType() {
        return "TINYINT";
    }

    @Override
    public String serialize(Byte p0) {
        return "'"+p0+"'";
    }

    @Override
    public Byte deserialize(String p0) {
        return Byte.parseByte(p0);
    }
}
