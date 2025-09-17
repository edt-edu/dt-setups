package dts.modelmanager.implementations
/**
 * Represents the structure and metadata of a database table.
 *
 * <p>This class is used to define and manage information about a specific table,
 * including its name, columns, data types, and optionally its data rows or constraints.</p>
 *
 * <p>It can be used for table creation, schema inspection, or as a model for interacting
 * with the database layer.</p>
 *
 * <p>Typical fields may include column names, types, primary key definitions, etc.
 * Implementation may vary depending on use case (e.g., code generation, SQL building, etc.).</p>
 */
abstract class DBTable<T> {
    /**
     * The name of the database table.
     */
    lateinit var tableName: String

    /**
     * A set of column names (headers) defined in the table.
     */
    lateinit var tableheader: MutableSet<String>

    /**
     * A map from column names to their corresponding data types (e.g., "id" -> "INT").
     */
    abstract var dataheaderAndType: MutableMap<String, T>

    /**
     * A set of column names that are marked as NOT NULL in the table schema.
     */
    lateinit var notnullableheader: MutableSet<String>

    /**
     * The name of the column serving as the primary key of the table.
     */
    lateinit var primaryKey: String

    /**
     * The data type of the primary key column.
     */
    abstract var primaryKeyType: T

    /**
     * A map of foreign key column names to their referenced table and column.
     *
     * <p>Each entry maps a foreign key in this table to a pair consisting of the referenced table name and referenced column name.</p>
     * For example: "user_id" -> ("users", "id")
     */
    lateinit var foreignKeysReferences: MutableMap<String, Pair<String, String>>
    /**
     * Constructs a new [DBTable] instance with the specified table structure and metadata.
     *
     * @param tableName the name of the table.
     * @param tableheader the set of column names defined in the table.
     * @param dataheaderAndType a map of column names to their corresponding data types.
     * @param notnullableheader a set of column names that are marked as NOT NULL.
     * @param primaryKey the name of the column used as the primary key.
     * @param primaryKeyType the data type of the primary key column.
     * @param foreignKeysReferences a map of foreign key column names to the referenced table and column.
     */
    constructor(
        tableName: String = "",
        tableheader: MutableSet<String> = mutableSetOf(),
        dataheaderAndType: MutableMap<String, T> = mutableMapOf(),
        notnullableheader: MutableSet<String> = mutableSetOf(),
        primaryKey: String = "",
        primaryKeyType: T,
        foreignKeysReferences:  MutableMap<String, Pair<String, String>> = mutableMapOf()
    ) {
        this.tableName = tableName
        this.tableheader = tableheader
        this.dataheaderAndType = dataheaderAndType
        this.notnullableheader = notnullableheader
        this.primaryKey = primaryKey
        this.primaryKeyType = primaryKeyType
        this.foreignKeysReferences = foreignKeysReferences
    }

    constructor()

    /**
     * Returns a string representation of the [DBTable], typically including the table name
     * and possibly schema details.
     *
     * @return a string summarizing the table's structure and metadata.
     */
    override fun toString(): String {
        return "Table: $tableName \n " +"\tPK: $primaryKey \t \n"+ "\tHeaders: $tableheader \n" + "\t\t \n"+ "\t\tNotNullables: $notnullableheader \n " + "\t\tForeignKeys: $foreignKeysReferences \n"
    }
}
