package lt.terzer.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestClass extends AbstractDatabaseSavable {

    private Date date;
    private List<Integer> list = new ArrayList<>();
    private String foo;
    private int number;
    private boolean buyer;

    public TestClass(){
        date = new Date();
        list.add(2);
        list.add(5);
        list.add(9);
        foo = "awsd";
        number = -12;
        buyer = true;
    }

    @Override
    public String toString() {
        return "[id=" + getId() + ", foo=" + foo + ", number=" + number + ", buyer=" + buyer + ", date=" + date + ", list=" + list.toString() + "]";
    }
}
