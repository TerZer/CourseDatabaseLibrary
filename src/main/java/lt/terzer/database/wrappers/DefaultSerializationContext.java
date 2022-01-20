package lt.terzer.database.wrappers;

import lt.terzer.database.TestClass;
import lt.terzer.database.annotations.NotSavable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultSerializationContext implements SerializationContext {

    private AdapterRegistry registry;

    public DefaultSerializationContext(AdapterRegistry adapterRegistry){
        registry = adapterRegistry;
    }


    @Override
    public <T> String serialize(T p0) {
        List<Field> fields = registry.getFields(p0.getClass());
        StringBuilder str = new StringBuilder();
        for(int i = fields.size()-1;i >= 0;i--){
            fields.get(i).setAccessible(true);
            final Class<?> klass = registry.wrapPrimitive(fields.get(i).getType());
            final Optional<Serializer<?>> oSerializer = this.registry.getSerializer(klass);
            if (oSerializer.isPresent()) {
                final Serializer serializer = oSerializer.get();
                try {
                    if(fields.get(i).getName().equalsIgnoreCase("id")){
                        if(((Integer)fields.get(i).get(p0)) == -1){
                            continue;
                        }
                    }
                    str.append(serializer.serialize(fields.get(i).get(p0)));
                    if(i > 0){
                        str.append(",");
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return str.toString();
    }

    @Override
    public <T> Map<String, String> getClassTypes(Class<T> p0) {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        List<Field> fields = registry.getFields(p0);
        for(Field field : fields){
            hashMap.put(field.getName(), getMysqlType(field.getType()));
        }
        return hashMap;
    }


    private String getMysqlType(Class<?> type) {
        final Class<?> klass = registry.wrapPrimitive(type);
        final Optional<Serializer<?>> oSerializer = this.registry.getSerializer(klass);
        if (oSerializer.isPresent()) {
            final Serializer serializer = oSerializer.get();
            return serializer.dataType();
        }
        return null;
    }
}
