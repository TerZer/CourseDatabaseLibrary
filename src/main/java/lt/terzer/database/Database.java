package lt.terzer.database;

import lt.terzer.database.wrappers.DefaultMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database<T extends DatabaseSavable> {


    private static final int RETRY_TIMES = 2;
    private DatabaseStatus status = DatabaseStatus.NOT_CONNECTED;
    private final String url, username, password;
    protected final String table;
    private boolean broken;
    private DefaultMapper defaultMapper = new DefaultMapper();
    private Class<T> clazz;

    public Database(String url, String database, String table, String username, Class<T> clazz){
        this(url, database, table, username, null, clazz);
    }

    public Database(String url, String database, String table, String username, String password, Class<T> clazz){
        this.clazz = clazz;
        database = database.endsWith("/") ? StringUtils.removeEnd(database, "/") : database;
        if(url.startsWith("jdbc:mysql://")) {
            if(url.endsWith("/"))
                this.url = url+database+"?createDatabaseIfNotExist=true";
            else
                this.url = url+"/"+database+"?createDatabaseIfNotExist=true";
        }
        else {
            if (url.endsWith("/"))
                this.url = "jdbc:mysql://" + url + database+"?createDatabaseIfNotExist=true";
            else
                this.url = "jdbc:mysql://" + url + "/" + database+"?createDatabaseIfNotExist=true";
        }
        this.table = table;
        this.username = username;
        this.password = password;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not find Driver!");
            status = DatabaseStatus.DRIVER_ERROR;
            broken = true;
            return;
        }
        close(connect());
    }

    protected Connection connect(){
        for(int i = 0;i < RETRY_TIMES;i++) {
            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                if(connection == null)
                    throw new SQLException("Could not get connection");
                if(connection.isClosed() || !connection.isValid(200))
                    throw new SQLException("Connection is not valid");
                status = DatabaseStatus.CONNECTED;
                return connection;
            } catch (SQLException e) {
                System.out.println("Could not connect to SQL! Reason: " + e.getMessage());
            }
        }
        status = DatabaseStatus.NOT_CONNECTED;
        return null;
    }

    private boolean tableExists(){
        Connection connection = connect();
        ResultSet resultSet = null;
        try {
            if(connection != null) {
                resultSet = connection.getMetaData().getTables(null, null, table, null);
                if (resultSet.next()) {
                    return resultSet.getString(3).equals(table);
                }
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
        finally {
            try {
                if(resultSet != null)
                    resultSet.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
            close(connection);
        }
    }

    public boolean createTable(){
        Connection connection = connect();
        Statement statement = null;
        try {
            if(connection != null) {
                statement = connection.createStatement();
                StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table + " (");
                Map<String, String> map = defaultMapper.getClassTypes(clazz);
                List<String> keys = new ArrayList<>(map.keySet());
                for(int i = keys.size()-1;i >= 0;i--){
                    if(keys.get(i).equalsIgnoreCase("id")){
                        sql.append(keys.get(i));
                        sql.append(" INT AUTO_INCREMENT PRIMARY KEY");
                    }
                    else {
                        sql.append(keys.get(i));
                        sql.append(" ").append(map.get(keys.get(i)));
                    }
                    if(i != 0){
                        sql.append(", ");
                    }
                }
                sql.append(")");
                statement.executeUpdate(sql.toString());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(statement != null){
                    statement.close();
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
            close(connection);
        }
        return false;
    }

    public void shutdown() {
        status = DatabaseStatus.NOT_CONNECTED;
    }

    protected boolean removeQuery(String where){
        if(!tableExists())
            createTable();
        Connection connection = connect();
        if(where == null || where.trim().equals(""))
            return false;
        if(connection == null)
            return false;
        if(!tableExists())
            return false;
        try {
            connection.createStatement().execute("DELETE FROM " + table + " WHERE " + where);
        } catch (SQLException e) {
            System.out.println("Could not delete from database " + e.getMessage());
            return false;
        }
        return true;
    }

    protected Pair<Connection, ResultSet> executeQuery(){
        if(!tableExists())
            createTable();
        return executeQuery(null);
    }

    protected Pair<Connection, ResultSet> executeQuery(String where){
        if(!tableExists())
            createTable();
        Connection connection = connect();
        if(connection == null) {
            return null;
        }
        if(!tableExists()) {
            return null;
        }
        ResultSet results = null;
        try {
            Statement statement = connection.createStatement();

            String sql = "SELECT * FROM " + table;
            if(where != null && !where.trim().equals(""))
                sql += " WHERE " + where;

            results = statement.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println("Could not retrieve data from the database " + e.getMessage());
        }
        return Pair.of(connection, results);
    }

    public List<T> get(){
        return retrieveData(executeQuery());
    }

    public List<T> get(String where){
        return retrieveData(executeQuery(where));
    }

    protected List<T> retrieveData(Pair<Connection, ResultSet> pair){
        List<T> list = new ArrayList<>();
        if(pair.getValue() != null){
            try {
                while (pair.getValue().next()) {
                    list.add(defaultMapper.deserialize(pair.getValue(), clazz));
                }
            } catch (SQLException e) {
                System.out.println("Could not retrieve data from the database " + e.getMessage());
            }
            finally {
                close(pair.getKey());
            }
        }
        return list;
    }

    private String idsToString(List<Integer> ids){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i < ids.size();i++){
            stringBuilder.append(ids.get(i));
            if(i+1 != ids.size())
                stringBuilder.append(", ");
        }
        return stringBuilder.toString();
    }

    public boolean remove(T... obj){
        return remove(Arrays.asList(obj));
    }

    public boolean remove(List<T> list){
        if(list.isEmpty()){
            return true;
        }
        return removeQuery("id in (" + idsToString(list.stream()
                .map(DatabaseSavable::getId).collect(Collectors.toList())) + ")");
    }

    public boolean remove(String where){
        return removeQuery(where);
    }

    public boolean save(T... obj){
        return save(Arrays.asList(obj));
    }

    public boolean save(List<T> list) {
        if(list.isEmpty())
            return true;
        if(broken)
            return false;
        Connection connection = connect();
        if(connection == null) {
            return false;
        }
        if(!tableExists()) {
            if(!createTable())
                return false;
        }
        return saveData(connection, list);
    }

    private boolean saveData(Connection connection, List<T> list){
        try {
            for(T obj : list){
                Statement stmt = connection.createStatement();

                StringBuilder sql = new StringBuilder("INSERT INTO "+table+" (");
                Map<String, String> map = defaultMapper.getClassTypes(clazz);
                List<String> keys = new ArrayList<>(map.keySet());
                for(int i = keys.size()-1;i >= 0;i--) {
                    if (obj.getId() == -1 && keys.get(i).equalsIgnoreCase("id")) {
                        continue;
                    }
                    sql.append(keys.get(i));
                    if(i > 0){
                        sql.append(", ");
                    }
                    else{
                        sql.append(") ");
                    }
                }

                sql.append("values (");
                sql.append(defaultMapper.serialize(obj));
                sql.append(")");

                if(obj.getId() == -1){
                    sql.append(";");
                    stmt.executeUpdate(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                }
                else{
                    sql.append(" ON DUPLICATE KEY UPDATE ");
                    for(int i = keys.size()-1;i >= 0;i--) {
                        sql.append(keys.get(i));
                        sql.append(" = VALUES(");
                        sql.append(keys.get(i));
                        sql.append(")");
                        if(i > 0){
                            sql.append(", ");
                        }
                        else{
                            sql.append(";");
                        }
                    }
                    stmt.executeUpdate(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                }
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    obj.setId(id);
                }
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void close(Connection connection){
        try {
            if(connection != null)
                connection.close();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public DatabaseStatus status() {
        return status;
    }

}
