package lt.terzer.database.wrappers;

import lt.terzer.database.DatabaseSavable;
import lt.terzer.database.annotations.NotSavable;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DefaultDeserializationContext implements DeserializationContext {

    private AdapterRegistry registry;

    public DefaultDeserializationContext(AdapterRegistry adapterRegistry){
        registry = adapterRegistry;
    }

    @Override
    public <T extends DatabaseSavable> T deserialize(ResultSet rs, Class<T> clazz) {
        try {
            ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
            Constructor objDef = Object.class.getDeclaredConstructor();
            Constructor intConstr = rf.newConstructorForSerialization(clazz, objDef);
            T obj = clazz.cast(intConstr.newInstance());
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String fieldName = rs.getMetaData().getColumnName(i);
                List<Field> fields = registry.getFields(clazz);
                for(Field field : fields){
                    if(field.getName().equalsIgnoreCase(fieldName)){
                        field.setAccessible(true);
                        if(List.class.isAssignableFrom(field.getType())){
                            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                            Class<T> typeClass = (Class<T>) genericType.getActualTypeArguments()[0];

                            final List<Object> newList = new ArrayList<>();
                            String str = (String) rs.getObject(i);
                            for (String s : str.split(",")) {
                                registry.getSerializer(typeClass).ifPresent(serializer -> newList.add(serializer.deserialize(s)));
                            }
                            field.set(obj, newList);
                        }
                        else {
                            field.set(obj, rs.getObject(i));
                        }
                    }
                }
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
