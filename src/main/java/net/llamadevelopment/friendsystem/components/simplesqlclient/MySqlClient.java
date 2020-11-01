package net.llamadevelopment.friendsystem.components.simplesqlclient;

import net.llamadevelopment.friendsystem.components.simplesqlclient.objects.SqlColumn;
import net.llamadevelopment.friendsystem.components.simplesqlclient.objects.SqlDocument;
import net.llamadevelopment.friendsystem.components.simplesqlclient.objects.SqlDocumentSet;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author LlamaDevelopment
 * @project SimpleSQLClient
 * @website http://llamadevelopment.net/
 */
public class MySqlClient {

    private Connection connection;
    private final String host, port, username, password, database;

    public MySqlClient(String host, String port, String user, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = user;
        this.password = password;
        this.database = database;
        this.connect();
    }

    public void connect() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useGmtMillisForDatetimes=true&serverTimezone=GMT", username, password);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void close() {
        try {
            this.connection.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void createTable(String name, SqlColumn columns) {
        try {
            StringBuilder columnsStringBuilder = new StringBuilder();

            for (String type : columns.get()) {
                columnsStringBuilder.append(type).append(", ");
            }

            String columnsString = columnsStringBuilder.substring(0, columnsStringBuilder.length() - 2);
            String statement = "CREATE TABLE IF NOT EXISTS " + name + "(" + columnsString + ");";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void createTable(String name, String primaryKey, SqlColumn columns) {
        try {
            StringBuilder columnsStringBuilder = new StringBuilder();

            for (String type : columns.get()) {
                columnsStringBuilder.append(type).append(", ");
            }

            String columnsString = columnsStringBuilder.toString();
            String statement = "CREATE TABLE IF NOT EXISTS " + name + "(" + columnsString + "PRIMARY KEY (" + primaryKey + "));";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void update(String table, SqlDocument search, SqlDocument updates) {
        this.update(table, search.first().getKey(), search.first().getValue(), updates);
    }

    public void update(String table, String searchKey, final Object searchValue, SqlDocument updates) {
        try {

            Object valueSearch = searchValue;
            if (valueSearch instanceof String) valueSearch = "'" + valueSearch + "'";

            StringBuilder updateBuilder = new StringBuilder();

            for (Map.Entry<String, Object> update : updates.getAll().entrySet()) {

                Object value = update.getValue();
                if (value instanceof String) value = "'" + value + "'";

                updateBuilder.append(update.getKey()).append(" = ").append(value).append(", ");
            }

            String update = updateBuilder.substring(0, updateBuilder.length() - 2);
            String statement = "UPDATE " + table + " SET " + update + " WHERE " + searchKey + " = " + valueSearch + ";";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void insert(String table, SqlDocument values) {
        try {
            StringBuilder valueNamesBuilder = new StringBuilder("(");
            StringBuilder valueDataBuilder = new StringBuilder("(");

            for (Map.Entry<String, Object> insert : values.getAll().entrySet()) {

                Object data = insert.getValue();
                if (data instanceof String) data = "'" + data + "'";

                valueNamesBuilder.append(insert.getKey()).append(", ");
                valueDataBuilder.append(data).append(", ");
            }

            String valueNames = valueNamesBuilder.substring(0, valueNamesBuilder.length() - 2);
            valueNames = valueNames + ")";

            String valueData = valueDataBuilder.substring(0, valueDataBuilder.length() - 2);
            valueData = valueData + ")";


            String statementString = "INSERT INTO " + table + " " + valueNames + " VALUES " + valueData + ";";
            PreparedStatement statement = connection.prepareStatement(statementString);
            statement.executeUpdate();
            statement.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void delete(String table, SqlDocument search) {
        this.delete(table, search.first().getKey(), search.first().getValue());
    }

    public void delete(String table, String key, Object value) {
        try {
            if (value instanceof String) value = "'" + value + "'";
            String statement = "DELETE FROM " + table + " WHERE " + key + " = " + value + ";";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public SqlDocumentSet find(String table, SqlDocument search) {
        return this.find(table, search.first().getKey(), search.first().getValue());
    }

    public SqlDocumentSet find(String table, String key, Object value) {
        try {
            if (value instanceof String) value = "'" + value + "'";
            String statement = "SELECT * FROM " + table + " WHERE " + key + " = " + value + ";";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData meta = resultSet.getMetaData();

            Set<SqlDocument> set = new HashSet<>();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();

                for (int i = 1; i <= meta.getColumnCount(); i++)
                    map.put(meta.getColumnName(i), resultSet.getObject(i));

                set.add(new SqlDocument(map));
            }

            preparedStatement.close();
            resultSet.close();
            return new SqlDocumentSet(set);

        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public SqlDocumentSet find(String table) {
        try {
            String statement = "SELECT * FROM " + table + ";";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData meta = resultSet.getMetaData();

            Set<SqlDocument> set = new HashSet<>();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();

                for (int i = 1; i <= meta.getColumnCount(); i++)
                    map.put(meta.getColumnName(i), resultSet.getObject(i));

                set.add(new SqlDocument(map));
            }

            preparedStatement.close();
            resultSet.close();
            return new SqlDocumentSet(set);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
