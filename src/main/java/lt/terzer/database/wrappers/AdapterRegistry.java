package lt.terzer.database.wrappers;

import java.lang.reflect.Field;
import java.util.*;

import com.google.common.collect.ImmutableMap;
import lt.terzer.database.annotations.NotSavable;
import lt.terzer.database.wrappers.adapters.TypeAdapter;

public class AdapterRegistry {

    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new ImmutableMap.Builder<Class<?>, Class<?>>().put(Boolean.TYPE, Boolean.class)
            .put(Byte.TYPE, Byte.class).put(Character.TYPE, Character.class)
            .put(Double.TYPE, Double.class).put(Float.TYPE, Float.class)
            .put(Integer.TYPE, Integer.class).put(Long.TYPE, Long.class)
            .put(Short.TYPE, Short.class).build();

    private final Map<Class<?>, Serializer<?>> serializerMap;

    public AdapterRegistry() {
        this.serializerMap = new HashMap<>();
    }
    public void registerAdapter(final Class<?> klass, final TypeAdapter<?> adapter) {
        /*if (adapter instanceof NBTDeserializer) {
            this.deserializerMap.put(klass, (NBTDeserializer<?>)adapter);
        }*/
        if (adapter instanceof Serializer) {
            this.serializerMap.put(klass, (Serializer<?>)adapter);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> wrapPrimitive(final Class<T> c) {
        return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
    }

    public Optional<Serializer<?>> getSerializer(final Class<?> klass) {
        return Optional.ofNullable(this.serializerMap.get(klass));
    }

    public List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            fields.removeIf(field -> field.getAnnotation(NotSavable.class) != null);
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
