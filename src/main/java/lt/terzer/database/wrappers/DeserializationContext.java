package lt.terzer.database.wrappers;

import lt.terzer.database.DatabaseSavable;

import java.sql.ResultSet;

interface DeserializationContext {

    <T extends DatabaseSavable> T deserialize(final ResultSet rs, final Class<T> clazz);

}
