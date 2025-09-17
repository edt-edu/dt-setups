package dts.modelmanager.implementations.postgres

import dts.modelmanager.implementations.DBTable

class PostgresDBTable(): DBTable<PostgresDataType>() {

    override lateinit var primaryKeyType: PostgresDataType
    override lateinit var dataheaderAndType: MutableMap<String, PostgresDataType>

    override fun toString(): String {
        return "Table: "+ this.tableName + ", PK: ${this.primaryKey}, type: $primaryKeyType : {}"
    }
}