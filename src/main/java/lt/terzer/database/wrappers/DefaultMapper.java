package lt.terzer.database.wrappers;

import lt.terzer.database.DatabaseSavable;
import lt.terzer.database.wrappers.adapters.*;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DefaultMapper {

    private SerializationContext serializationContext;
    private DeserializationContext deserializationContext;

    public DefaultMapper(){
        this(new AdapterRegistry());
    }

    public DefaultMapper(AdapterRegistry adapters){
        adapters.registerAdapter(Byte.class, new ByteAdapter());
        adapters.registerAdapter(Date.class, new DateAdapter());
        adapters.registerAdapter(Integer.class, new IntegerAdapter());
        adapters.registerAdapter(Double.class, new DoubleAdapter());
        adapters.registerAdapter(String.class, new StringAdapter());
        adapters.registerAdapter(Boolean.class, new BooleanAdapter());
        adapters.registerAdapter(List.class, new ListAdapter(adapters));
        this.serializationContext = new DefaultSerializationContext(adapters);
        this.deserializationContext = new DefaultDeserializationContext(adapters);
    }

    public <T> Map<String, String> getClassTypes(Class<T> klass) {
        return serializationContext.getClassTypes(klass);
    }

    public <T> String serialize(T p0){
        return serializationContext.serialize(p0);
    }

    public <T extends DatabaseSavable> T deserialize(ResultSet rs, Class<T> clazz) {
        return deserializationContext.deserialize(rs, clazz);
    }
}
