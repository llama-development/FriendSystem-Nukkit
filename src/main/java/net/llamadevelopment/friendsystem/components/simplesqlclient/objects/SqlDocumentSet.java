package net.llamadevelopment.friendsystem.components.simplesqlclient.objects;

import java.util.Set;

/**
 * @author LlamaDevelopment
 * @project SimpleSQLClient
 * @website http://llamadevelopment.net/
 */
public class SqlDocumentSet {

    private final Set<SqlDocument> results;

    public SqlDocumentSet(Set<SqlDocument> results) {
        this.results = results;
    }

    public Set<SqlDocument> getAll() {
        return results;
    }

    public SqlDocument first() {
        return results.iterator().hasNext() ? results.iterator().next() : null;
    }
}
