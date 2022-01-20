package lt.terzer.database.wrappers;

import lt.terzer.database.wrappers.adapters.TypeAdapter;

public interface Serializer<T> extends TypeAdapter<T> {
    String dataType();
    String serialize(final T p0);
    T deserialize(String p0);
}
