package lt.terzer.database.wrappers.adapters;

import lt.terzer.database.wrappers.Serializer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAdapter implements Serializer<Date> {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String dataType() {
        return "DATETIME";
    }

    @Override
    public String serialize(Date p0) {
        return "'"+format.format(p0)+"'";
    }

    @Override
    public Date deserialize(String p0) {
        try {
            return format.parse(p0);
        } catch (ParseException e) {
            return null;
        }
    }
}