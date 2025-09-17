package dts.modelmanager.implementations

/**
 * Container class for holding metadata about all database tables.
 *
 * <p>This class serves as a registry or snapshot of the database schema,
 * mapping table names to their corresponding [DBTable] definitions.</p>
 */
class DatabaseMetaInf<T> {

    /**
     * A map of table names to their associated [DBTable] objects.
     *
     * <p>This provides access to the structure and constraints of each table
     * in the connected database.</p>
     */
    lateinit var databaseTables: Map<String, DBTable<T>>
}


