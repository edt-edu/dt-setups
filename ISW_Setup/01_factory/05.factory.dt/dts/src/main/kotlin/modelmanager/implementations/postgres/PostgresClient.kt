package dts.modelmanager.implementations.postgres

import dts.modelmanager.PropertiesReader
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

/**
 * Manages interactions with a PostgreSQL database.
 * <p>
 * This class provides functionality for:
 * <ul>
 *   <li>Loading database configuration settings from a properties file</li>
 *   <li>Establishing and closing database connections</li>
 *   <li>Retrieving metadata such as table names and column headers</li>
 *   <li>Performing basic SQL operations: SELECT, INSERT, UPDATE, DELETE</li>
 *   <li>Dynamically creating tables and building SQL statements</li>
 * </ul>
 * <p>
 * It maintains in-memory representations of database tables and supports typed
 * data extraction and flexible filtering.
 *
 * <p><b>Note:</b> This class is designed for use with PostgreSQL and assumes a JDBC-compatible setup.</p>
 *
 * @author JZ
 * @since 2025.05.16
 */
class PostgresClient {
    /**
     * Reader for application configuration properties.
     * Used to load values such as database connection details from external property files.
     */
    lateinit var cfgRdr: PropertiesReader

    /**
     * JDBC connection object for connecting to the PostgreSQL database.
     */
    lateinit var c: Connection

    /**
     * JDBC statement object used to execute SQL queries against the database.
     */
    lateinit var stmt: Statement

    /**
     * Filename of the PostgreSQL configuration file or resource.
     */
    lateinit var postgresFilename: String

    /**
     * JDBC URL string for connecting to the PostgreSQL database.
     */
    lateinit var postgresurl: String

    /**
     * Username for authenticating with the PostgreSQL database.
     */
    lateinit var postgresuser: String

    /**
     * Password for the specified PostgreSQL database user.
     */
    lateinit var postgresuerpassword: String

    /**
     * A mutable map of table names to their corresponding [DBTable] definitions.
     * Used to store metadata or runtime representations of database tables.
     */
    var dbTables: MutableMap<String, PostgresDBTable> = mutableMapOf()
    /**
     * Loads PostgreSQL settings from the specified properties file.
     *
     * @param filepath the path to the properties file (default: "postgres.properties").
     */
    fun loadPostgresSettings(filepath: String = "postgres.properties") {

        cfgRdr = PostgresConfigProperties(filepath)

        this.postgresFilename = cfgRdr.getProperty("postgresFilename")
        this.postgresurl = cfgRdr.getProperty("postgresurl")
        this.postgresuser = cfgRdr.getProperty("postgresuser")
        this.postgresuerpassword = cfgRdr.getProperty("postgresuerpassword")

        try {
            this.connect()
            this.disconnect()
        }
        catch (e: SQLException) {
            this.disconnect()
        }
    }
    /**
     * Initializes the database manager by connecting to the database and retrieving metadata.
     */
    fun initDBMGR() {
        getAllTablesAndHeader()
    }
    /**
     * Retrieves all table names and their corresponding headers (columns) from the database.
     */
    private fun getAllTablesAndHeader() {
        try {

            connect()
            c.autoCommit = true
            stmt = c.createStatement()

            var sqlstr: String = "SELECT t.table_name,array_agg(c.column_name::text) as columns \n" +
                    "from information_schema.tables t \n" +
                    "inner join information_schema.columns c on t.table_name = c.table_name \n" +
                    "where \n" +
                    "t.table_schema = \'public\' \n" +
                    "and t.table_type= \'BASE TABLE\' \n" +
                    "and c.table_schema = \'public\' \n" +
                    "group by t.table_name;"

            var rs = stmt.executeQuery(sqlstr)

            while (rs.next()) {
                val stmt2 = c.createStatement()
                // get all tables and columns
                var tableInDB = PostgresDBTable()
                tableInDB.dataheaderAndType = mutableMapOf()
                tableInDB.tableheader = mutableSetOf()
                tableInDB.notnullableheader = mutableSetOf()
                tableInDB.foreignKeysReferences = mutableMapOf()
                val table_name = rs.getString("table_name")
                val name = rs.getString("columns")

                tableInDB.tableName = table_name

                var splittableString = name.substring(1, name.length - 1)
                var tablesheaders = splittableString.split(',')
                tableInDB.tableheader = tablesheaders.toMutableSet()

                //get all datatypes and nullables
                val sqlstr2 = "SELECT \n" +
                        "column_name, \n" +
                        "is_nullable, \n" +
                        "data_type \n" +
                        "FROM \n" +
                        "information_schema.columns \n" +
                        "WHERE \n" +
                        "table_name = \'$table_name\';"

                val rs2 = stmt2.executeQuery(sqlstr2)
                while (rs2.next()) {
                    val id = rs2.getString("column_name")
                    val name = PostgresDataType.fromAny(rs2.getString("data_type"))

                    val nullable = rs2.getString("is_nullable")

                    if (name != null) {
                        tableInDB.dataheaderAndType.put(id, name)
                    }

                    if (nullable.equals("YES"))
                        tableInDB.notnullableheader.add(id)

                }

                rs2.close()

                //get all primarykeys
                val sqlstr3 = "SELECT a.attname, format_type(a.atttypid, a.atttypmod) AS data_type\n" +
                        "FROM   pg_index i\n" +
                        "JOIN   pg_attribute a ON a.attrelid = i.indrelid\n" +
                        "                     AND a.attnum = ANY(i.indkey)\n" +
                        "WHERE  i.indrelid = \'$table_name\'::regclass\n" +
                        "AND    i.indisprimary;"
                val rs3 = stmt2.executeQuery(sqlstr3)
                while (rs3.next()) {
                    val id = rs3.getString("attname")
                    val name = PostgresDataType.fromSqlType(rs3.getString("data_type"))

                    tableInDB.primaryKey = id
                    if (name != null) {
                        tableInDB.primaryKeyType = name
                    }
                    tableInDB.tableheader.remove(id)
                    tableInDB.dataheaderAndType.remove(id)
                    tableInDB.notnullableheader.remove(id)
                }
                rs3.close()


                //get all foreign keys
                val sqlstr4 = "select\n" +
                        "conrelid::regclass table_name,\n" +
                        "a1.attname column_name,\n" +
                        "confrelid::regclass referenced_table,\n" +
                        "a2.attname referenced_column,\n" +
                        "conname constraint_name\n" +
                        "from (\n" +
                        "select conname, conrelid::regclass, confrelid::regclass, col, fcol\n" +
                        "from pg_constraint c,\n" +
                        "lateral unnest(conkey) col,\n" +
                        "lateral unnest(confkey) fcol\n" +
                        "where contype = 'f'  -- foreign keys constraints\n" +
                        ") s\n" +
                        "join pg_attribute a1 on a1.attrelid = conrelid and a1.attnum = col\n" +
                        "join pg_attribute a2 on a2.attrelid = confrelid and a2.attnum = fcol where conrelid::regclass = '$table_name'::regclass;"

                val rs4 = stmt2.executeQuery(sqlstr4)
                while (rs4.next()) {
                    val id = rs4.getString("column_name")
                    val name = rs4.getString("table_name")
                    val refCol = rs4.getString("referenced_column")
                    val reftable = rs4.getString("referenced_table")

                    tableInDB.foreignKeysReferences[id] = Pair(reftable, refCol)

                }
                rs4.close()

                println(tableInDB)
                dbTables.put(table_name, tableInDB)
            }

            rs.close()
            disconnect()
        } catch (e: Exception) {
            disconnect()
        }
    }
    /**
     * Establishes a connection to the PostgreSQL database using the loaded settings.
     */
    private fun connect() {

        try {
            Class.forName(postgresFilename)
            c = DriverManager
                .getConnection(postgresurl, postgresuser, postgresuerpassword)
        } catch (sqle: PSQLException) {
            System.out.println(sqle.toString())
        }

    }
    /**
     * Closes the active connection to the database.
     */
    private fun disconnect() {
        if (this::stmt.isInitialized) stmt.close()
        c.close()
    }
    /**
     * Deletes rows from the specified table that match the given condition.
     *
     * @param tablename the name of the table.
     * @param whereValues a map of field-value pairs representing the WHERE condition.
     */
    fun deleteData(tablename: String, whereValues: MutableMap<String, String>) {
        try {
            connect()
            c.autoCommit = true
            println("Opened database successfully")

            stmt = c.createStatement()


            var wherestatement = "where "
            for (key in whereValues.keys) {
                wherestatement += key + " = " + whereValues[key] + "AND "
            }
            wherestatement = wherestatement.substring(0, wherestatement.length - 4)
            wherestatement += ";"

            val sql = "DELETE from $tablename "+ wherestatement
            stmt.executeUpdate(sql)

            disconnect()
        } catch (e: java.lang.Exception) {
            System.err.println(e.javaClass.name + ": " + e.message)
            disconnect()
        }
        println("Operation done successfully")
    }

    /**
     * Updates rows in the specified table that match the given condition.
     *
     * @param tablename the name of the table.
     * @param whereValues a map of field-value pairs for the WHERE clause.
     * @param setValues a map of field-value pairs to update.
     */
    fun updateData(tablename: String, whereValues: MutableMap<String, String>, setValues: MutableMap<String, String>) {
        try {
            connect()
            c.autoCommit = true
            println("Opened database successfully")

            stmt = c.createStatement()
            var setstatement = "set "
            for (key in setValues.keys) {
                setstatement += key + " = " + setValues[key] + ", "
            }
            setstatement = setstatement.substring(0, setstatement.length - 2)
            setstatement += "\n"

            var wherestatement = "where "
            for (key in whereValues.keys) {
                wherestatement += key + " = " + whereValues[key] + "AND "
            }
            wherestatement = wherestatement.substring(0, wherestatement.length - 4)
            wherestatement += ";"

            val sql = "UPDATE $tablename " + setstatement + wherestatement
            stmt.executeUpdate(sql)
            disconnect()
        } catch (e: java.lang.Exception) {
            System.err.println(e.javaClass.name + ": " + e.message)
            disconnect()
        }
        println("Operation done successfully")
    }
    /**
     * Selects a single data point (row) from the specified table that matches the condition.
     *
     * @param tablename the name of the table.
     * @param condition a map of field-value pairs for filtering the rows.
     * @param filters a list of column names to include in the result.
     * @return a map representing the retrieved row (column to value).
     */
    fun selectOneDatapoint(
        tablename: String,
        condition: Map<String, String>, filters: List<String>
    ): MutableMap<String, Any> {
        var rows = selectMultipleRows(tablename, condition)
        var out = mutableMapOf<String, Any>()
        for (filter in filters) {
            out.put(filter, rows.get(filter)!!)
        }
        return out
    }
    /**
     * Selects multiple rows from the specified table that match the given condition.
     *
     * @param tablename the name of the table.
     * @param condition a map of field-value pairs for filtering the rows.
     * @return a nested map where the outer key is a unique identifier and the inner map contains column-value pairs.
     */
    fun selectMultipleRows(
        tablename: String,
        condition: Map<String, String>
    ): MutableMap<String, MutableMap<Any, Any>> {
        var output = mutableMapOf<String, MutableMap<Any, Any>>()
        try {
            connect()
            c.autoCommit = true
            stmt = c.createStatement()
            var sqlString = "SELECT * FROM $tablename WHERE \n"
            for (key in condition.keys) {
                sqlString += (key + " = \'" + condition[key] + "\'\nAND ")
            }
            sqlString = sqlString.substring(0, sqlString.length - 4)
            sqlString += ";"

            var rs = stmt.executeQuery(sqlString)
            var md = rs.metaData
            var row = mutableMapOf<Any, Any>()
            while (rs.next()) {

                for (i in 1..md.columnCount)
                    row.put(md.getColumnName(i), rs.getObject(i))

            }
            output.put(tablename, row)
            disconnect()
        } catch (e: java.lang.Exception) {
            disconnect()
            e.printStackTrace()
        }
        return output

    }

    /**
     * Selects specific columns from a table by their names.
     *
     * @param tablename the name of the table.
     * @param columns a list of column names to retrieve.
     * @return a pair where the first element is a message and the second is a map from column names to their values.
     */
    fun selectTableColumnsByNames(
        tablename: String,
        columns: List<String>
    ): Pair<String, MutableMap<String, MutableList<Any>>> {
        var mm: MutableMap<String, String> = mutableMapOf()

        try {
            connect()
            c.autoCommit = true
            stmt = c.createStatement()
            //get all datatypes and nullables
            val sqlstr2 = "SELECT \n" +
                    "column_name, \n" +
                    "data_type \n" +
                    "FROM \n" +
                    "information_schema.columns \n" +
                    "WHERE \n" +
                    "table_name = \'$tablename\';"

            val rs2 = stmt.executeQuery(sqlstr2)
            var columnTypes: MutableMap<String, String> = mutableMapOf()
            while (rs2.next()) {
                val id = rs2.getString("column_name")
                val name = rs2.getString("data_type")

                columnTypes.put(id, name)
            }
            rs2.close()

            for (column in columns) {
                mm.put(column, columnTypes.get(column)!!)
            }

            disconnect()
        } catch (e: Exception) {

            disconnect()
        }

        return selectTableColumns(tablename, mm)
    }
    /**
     * Selects specific columns and their typed values from a table.
     *
     * @param tablename the name of the table.
     * @param columnAndType a map from column names to their data types.
     * @return a pair containing a message and a map from column names to lists of typed values.
     */
    fun selectTableColumns(
        tablename: String,
        columnAndType: Map<String, String>
    ): Pair<String, MutableMap<String, MutableList<Any>>> {
        var outputTable: Pair<String, MutableMap<String, MutableList<Any>>> = Pair(tablename, mutableMapOf())

        try {
            connect()
            c.autoCommit = true
            println("Opened database successfully")

            stmt = c.createStatement()
            val rs = stmt.executeQuery("SELECT * FROM $tablename ;")
            var columnMap = mutableMapOf<String, MutableList<Any>>()
            for (key in columnAndType.keys) {
                columnMap.put(key, mutableListOf())
            }
            while (rs.next()) {

                for (key in columnAndType.keys) {
                    if (columnAndType.get(key).equals("INT"))
                        columnMap[key]!!.add(rs.getInt(key))
                    else if (columnAndType.get(key).equals("LONG"))
                        columnMap[key]!!.add(rs.getLong(key))
                    else if (columnAndType.get(key).equals("FLOAT"))
                        columnMap[key]!!.add(rs.getFloat(key))
                    else if (columnAndType.get(key).equals("DOUBLE"))
                        columnMap[key]!!.add(rs.getDouble(key))
                    else if (columnAndType.get(key).equals("STRING"))
                        columnMap[key]!!.add(rs.getString(key))
                    else if (columnAndType.get(key).equals("BLOB"))
                        columnMap[key]!!.add(rs.getBlob(key))
                    else columnMap[key]!!.add(rs.getObject(key))
                }

            }
            outputTable = Pair(tablename, columnMap)
            rs.close()
            disconnect()
        } catch (e: java.lang.Exception) {
            System.err.println(e.javaClass.name + ": " + e.message)
            disconnect()
        }
        println("Operation done successfully")

        return outputTable

    }
    /**
     * Inserts a new row into the specified table.
     *
     * @param tablename the name of the table.
     * @param fieldValuePairs a map of field-value pairs to insert.
     */
    fun insertData(tablename: String, fieldValuePairs: Map<String, String>) {
        try {
            connect()
            c.autoCommit = true
            println("Opened database successfully")

            stmt = c.createStatement()
            var sql = buildSQLStatementInsert(tablename, fieldValuePairs).toString()
            System.out.println(sql)
            stmt.executeUpdate(sql)

            stmt.close()
            disconnect()
        } catch (e: java.lang.Exception) {
            System.err.println(e.javaClass.name + ": " + e.message)
            disconnect()
        }
        println("Records created successfully")

    }

    /**
     * Creates a new table in the database using the provided [DBTable] structure.
     *
     * @param table the table definition including name, fields, and types.
     */
    fun createTable(table: PostgresDBTable) {
        try {
            connect()
            stmt = c.createStatement()

            var sql: String = buildSQLStatementCreate(table).toString()

            stmt.executeUpdate(sql)
            dbTables.put(table.tableName, table)

            disconnect()
        } catch (e: Exception) {
            System.err.println(e)
            disconnect()
        }
        System.out.println("Table created successfully")
    }
    /**
     * Builds a SQL CREATE TABLE statement for the given table definition.
     *
     * @param table the table definition.
     * @return a [StringBuilder] containing the CREATE TABLE SQL statement.
     */
    private fun buildSQLStatementCreate(table: PostgresDBTable): StringBuilder {

        //build statement "CREATE TABLE NAME"
        var tablename = table.tableName
        //build statement "NAME TYPE PRIMARY KEY NOT NULL"
        var primarykey = table.primaryKey
        var primarykeyType = table.primaryKeyType

        var notnullableFieldsWithType = mutableMapOf<String, String>()
        var nullableFieldsWithType = mutableMapOf<String, String>()
        var foreignFieldsWithType = mutableMapOf<String, String>()
        var foreignFieldsTableNames = mutableMapOf<String, Pair<String, String>>()

        for (entry in table.tableheader) {
            if (table.foreignKeysReferences.keys.contains(entry)) {
                //build statement "NAME TYPE FOREIGN KEY NOT NULL"
                foreignFieldsWithType.put(entry, table.dataheaderAndType.get(entry)!!.toString())
                foreignFieldsTableNames.put(entry, table.foreignKeysReferences.get(entry)!!)
            } else if (table.notnullableheader.contains(entry)) {
                // build statement "NAME TYPE NOT NULL,"
                notnullableFieldsWithType.put(entry, table.dataheaderAndType.get(entry)!!.toString())
            } else {
                // build statement "NAME TYPE,"
                nullableFieldsWithType.put(entry, table.dataheaderAndType.get(entry)!!.toString())
            }
        }

        //build SQL syntax
        var sqlstaement = StringBuilder()
        sqlstaement.append("CREATE TABLE $tablename")
        sqlstaement.append("\n(")
        sqlstaement.append("$primarykey $primarykeyType " + "PRIMARY KEY" + " NOT NULL,\n")
        for (tableheaderName in foreignFieldsWithType.keys) {
            sqlstaement.append(
                "$tableheaderName " + foreignFieldsWithType.get(tableheaderName) + " " + "REFERENCES" + " "
                        + foreignFieldsTableNames.get(tableheaderName)!!.first
                        + "(" + foreignFieldsTableNames.get(tableheaderName)!!.second + "),\n"
            )
        }
        for (tableheaderName in notnullableFieldsWithType.keys) {
            sqlstaement.append("" + tableheaderName + " " + notnullableFieldsWithType.get(tableheaderName)!! + " " + "NOT NULL,\n")
        }

        for (tableheaderName in nullableFieldsWithType.keys) {
            sqlstaement.append("" + tableheaderName + " " + nullableFieldsWithType.get(tableheaderName)!! + ",\n")
        }
        sqlstaement.deleteCharAt(sqlstaement.length - 2)
        sqlstaement.append(")")

        return sqlstaement
    }
    /**
     * Builds a SQL INSERT statement for inserting data into the given table.
     *
     * @param tablename the name of the table.
     * @param fieldValuePairs the field-value pairs to insert.
     * @return a [StringBuilder] containing the INSERT SQL statement.
     */
    private fun buildSQLStatementInsert(tablename: String, fieldValuePairs: Map<String, String>): StringBuilder {
        var fields: MutableList<String> = mutableListOf()
        var values: MutableList<String> = mutableListOf()
        for (key in fieldValuePairs.keys) {
            fields.add(key)
            values.add(fieldValuePairs.get(key)!!)
        }

        System.out.println(fields)
        System.out.println(values)

        var sqlstaement = StringBuilder()
        sqlstaement.append("INSERT INTO $tablename ")
        sqlstaement.append("(")
        for (field in fields) sqlstaement.append(field + ",")
        sqlstaement.deleteCharAt(sqlstaement.length - 1)
        sqlstaement.append(") VALUES (")
        for (value in values) sqlstaement.append(value + ",")
        sqlstaement.deleteCharAt(sqlstaement.length - 1)
        sqlstaement.append(");")

        return sqlstaement
    }
}