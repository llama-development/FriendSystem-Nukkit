package net.llamadevelopment.friendsystem.components.simplesqlclient.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LlamaDevelopment
 * @project SimpleSQLClient
 * @website http://llamadevelopment.net/
 */
public class SqlDocument {

    private final Map<String, Object> data = new HashMap<>();

    public SqlDocument(String key, Object value) {
        data.put(key, value);
    }

    public SqlDocument(Map<String, Object> data) {
        this.data.putAll(data);
    }

    public Map<String, Object> getAll() {
        return this.data;
    }

    public Map.Entry<String, Object> first() {
        return data.entrySet().iterator().next();
    }

    public SqlDocument append(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public String getString(String key) {
        return (String) this.data.get(key);
    }

    public int getInt(String key) {
        return (int) this.data.get(key);
    }

    public long getLong(String key) {
        return (long) this.data.get(key);
    }

    public double getDouble(String key) {
        return (double) this.data.get(key);
    }

    public float getFloat(String key) {
        return (float) this.data.get(key);
    }

    public boolean getBoolean(String key) {
        return  (boolean) this.data.get(key);
    }

    public Object getObject(String key) {
        return this.data.get(key);
    }
}
