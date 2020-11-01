package net.llamadevelopment.friendsystem.components.simplesqlclient.objects;

import java.util.LinkedList;

/**
 * @author LlamaDevelopment
 * @project SimpleSQLClient
 * @website http://llamadevelopment.net/
 */
public class SqlColumn {

    private LinkedList<String> columns = new LinkedList<>();

    public SqlColumn(String name, String type) {
        columns.add(name + " " + type.toLowerCase());
    }

    public SqlColumn(String name, String type, int size) {
        columns.add(name + " " + type.toLowerCase() + "(" + size + ")");
    }

    public SqlColumn(String name, String type, int sizeFrom, int sizeTo) {
        columns.add(name + " " + type.toLowerCase() + "(" + sizeFrom + "," + sizeTo + ")");
    }

    public SqlColumn append(String name, String type) {
        columns.add(name + " " + type.toLowerCase());
        return this;
    }

    public SqlColumn append(String name, String type, int size) {
        columns.add(name + " " + type.toLowerCase() + "(" + size + ")");
        return this;
    }

    public SqlColumn append(String name, String type, int sizeFrom, int sizeTo) {
        columns.add(name + " " + type.toLowerCase() + "(" + sizeFrom + "," + sizeTo + ")");
        return this;
    }

    public LinkedList<String> get() {
        return this.columns;
    }

    public static class Type {
        public final static String INT = "INT";
        public final static String TINYINT = "TINYINT";
        public final static String SMALLINT = "SMALLINT";
        public final static String MEDIUMINT = "MEDIUMINT";
        public final static String BIGINT = "BIGINT";
        public final static String LONG = "BIGINT";
        public final static String FLOAT = "FLOAT";
        public final static String DOUBLE = "DOUBLE";
        public final static String DECIMAL = "DECIMAL";
        public final static String DATE = "DATE";
        public final static String DATETIME = "DATETIME";
        public final static String TIMESTAMP = "TIMESTAMP";
        public final static String TIME = "TIME";
        public final static String YEAR = "YEAR";
        public final static String CHAR = "CHAR";
        public final static String VARCHAR = "VARCHAR";
        public final static String BLOB = "BLOB";
        public final static String TEXT = "TEXT";
        public final static String TINYBLOB = "TINYBLOB";
        public final static String TINYTEXT = "TINYTEXT";
        public final static String MEDIUMBLOB = "MEDIUMBLOB";
        public final static String MEDIUMTEXT = "MEDIUMTEXT";
        public final static String LONGBLOB = "LONGBLOB";
        public final static String LONGTEXT = "LONGTEXT";
        public final static String ENUM = "ENUM";
    }

}
