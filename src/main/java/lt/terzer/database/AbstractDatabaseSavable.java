package lt.terzer.database;

public class AbstractDatabaseSavable implements DatabaseSavable {

    private int id = -1;

    public AbstractDatabaseSavable(){}
    public AbstractDatabaseSavable(int id){
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
}
