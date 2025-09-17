package dts.modelmanager.implementations.postgres

import dts.modelmanager.implementations.DBTable

fun createTable(pgm: PostgresClient) {
    // create table Test
    val dbTable = PostgresDBTable()
    dbTable.tableName = "DTTestTable22"
    dbTable.tableheader = mutableSetOf("valcolumn", "valcolumn2")

    dbTable.dataheaderAndType = mutableMapOf<String, PostgresDataType>()
    dbTable.dataheaderAndType["valcolumn"] = PostgresDataType.fromSqlType("VARCHAR") ?: PostgresDataType.VARCHAR
    dbTable.dataheaderAndType["valcolumn2"] = PostgresDataType.fromSqlType("INTEGER") ?: PostgresDataType.VARCHAR

    dbTable.notnullableheader = mutableSetOf()
    dbTable.notnullableheader.add("valcolumn")

    dbTable.primaryKey = "primaryKey1"
    dbTable.primaryKeyType = PostgresDataType.fromSqlType("INTEGER") ?: PostgresDataType.VARCHAR

    dbTable.foreignKeysReferences = mutableMapOf()
    dbTable.foreignKeysReferences.put("valcolumn2", Pair("DTTestTable", "primaryKey1"))

    pgm.createTable(dbTable)
}

fun insert1(pgm: PostgresClient) {
    //insert
    var insertmap = mutableMapOf<String, String>()
    insertmap.put("primaryKey1", "111")
    insertmap.put("valcolumn", "\'Testdata\'")
    insertmap.put("valcolumn2", "111")
    pgm.insertData("DTTestTable", insertmap)
}

fun insert(pgm: PostgresClient) {
    //insert
    var insertmap = mutableMapOf<String, String>()
    insertmap.put("primaryKey1", "1")
    insertmap.put("valcolumn", "\'Testdata\'")
    insertmap.put("valcolumn2", "111")
    pgm.insertData("DTTestTable22", insertmap)
}

fun select1(pgm: PostgresClient){
    //select certain rows
    var rows = mutableMapOf<String, String>()
    rows.put("valcolumn", "Testdata")
    rows.put("valcolumn2", "111")
    var out = pgm.selectMultipleRows("DTTestTable22", rows)
    println(out)
}

fun select2(pgm: PostgresClient){
    // select columns test
    var columns:MutableMap<String, String> = mutableMapOf()
    columns.put("primarykey1", "INTEGER")
    columns.put("valcolumn2", "")
    print(pgm.selectTableColumns("DTTestTable22", columns))
}

fun select3(pgm: PostgresClient){// select by name only
    var select:MutableList<String> = mutableListOf()
    select.add("primarykey1")
    select.add("valcolumn2")
    print(pgm.selectTableColumnsByNames("DTTestTable22", select))}

fun delete(pgm: PostgresClient){
    //delete data
    pgm.deleteData("DTTestTable22", mutableMapOf(Pair("primaryKey1", "1")))


}

fun update(pgm: PostgresClient){
    //update
    pgm.updateData("DTTestTable22",  mutableMapOf(Pair("primaryKey1", "1")), mutableMapOf(Pair("valcolumn", "\'Nothing\'")))

}

fun main() {

    val pgm = PostgresClient()
    pgm.loadPostgresSettings("postgreslxzg.properties")

    // init test
    pgm.initDBMGR()

    for (tbl in pgm.dbTables){
        println(tbl)
    }

    //createTable(pgm)
    //insert1(pgm)
    insert(pgm)

    select1(pgm)
    //select2(pgm)
    //select3(pgm)

    update(pgm)

    delete(pgm)
}

//command to clear cache::
//SELECT pg_terminate_backend(pg_stat_activity.pid)
//FROM pg_stat_activity
//WHERE pg_stat_activity.datname = 'digitaltwindb'
//AND pid <> pg_backend_pid();