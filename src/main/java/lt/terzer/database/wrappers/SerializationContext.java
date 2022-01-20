package lt.terzer.database.wrappers;

import java.util.HashMap;
import java.util.Map;

public interface SerializationContext {

    <T> String serialize(final T p0);
    <T> Map<String, String> getClassTypes(final Class<T> p0);

}
