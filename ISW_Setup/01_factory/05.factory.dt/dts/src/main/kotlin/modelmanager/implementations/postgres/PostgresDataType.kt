package dts.modelmanager.implementations.postgres

import java.sql.Types

/**
 * Enum representing PostgreSQL data types as used in CREATE TABLE statements.
 * Provides a strict 1:1 mapping to JDBC types.
 */
enum class PostgresDataType(
    val sqlTypeName: String,
    val jdbcType: Int,
    val description: String
) {

    // === Boolean ===
    BOOLEAN("boolean", Types.BOOLEAN, "Boolean value (true/false)"),

    // === Character / String Types ===
    CHAR("char", Types.CHAR, "Fixed-length character string"),
    VARCHAR("varchar", Types.VARCHAR, "Variable-length character string"),
    TEXT("text", Types.LONGVARCHAR, "Unbounded text string"),

    // === Numeric Types ===
    SMALLINT("smallint", Types.SMALLINT, "16-bit integer"),
    INTEGER("integer", Types.INTEGER, "32-bit integer"),
    BIGINT("bigint", Types.BIGINT, "64-bit integer"),
    DECIMAL("decimal", Types.DECIMAL, "Fixed precision numeric"),
    NUMERIC("numeric", Types.NUMERIC, "Fixed precision numeric"),
    REAL("real", Types.REAL, "Single-precision float"),
    DOUBLE_PRECISION("double precision", Types.DOUBLE, "Double-precision float"),
    SERIAL("serial", Types.INTEGER, "Auto-incrementing 32-bit integer"),
    BIGSERIAL("bigserial", Types.BIGINT, "Auto-incrementing 64-bit integer"),

    // === Temporal Types ===
    DATE("date", Types.DATE, "Date without time"),
    TIME("time", Types.TIME, "Time without timezone"),
    TIMESTAMP("timestamp", Types.TIMESTAMP, "Date and time without timezone"),
    TIMESTAMPTZ("timestamp with time zone", Types.TIMESTAMP_WITH_TIMEZONE, "Date and time with timezone"),
    INTERVAL("interval", Types.OTHER, "Time interval"),

    // === UUID ===
    UUID("uuid", Types.OTHER, "Universally Unique Identifier"),

    // === Array ===
    ARRAY("array", Types.ARRAY, "Array of any base type (e.g., text[], int[])"),

    // === JSON ===
    JSON("json", Types.OTHER, "Text-based JSON data"),
    JSONB("jsonb", Types.OTHER, "Binary JSON data"),

    // === HSTORE ===
    HSTORE("hstore", Types.OTHER, "Key-value pair store"),

    // === Binary ===
    BYTEA("bytea", Types.BINARY, "Binary data (byte array)"),

    // === Network ===
    INET("inet", Types.OTHER, "IPv4 or IPv6 address"),
    CIDR("cidr", Types.OTHER, "IP network block"),
    MACADDR("macaddr", Types.OTHER, "MAC address"),
    MACADDR8("macaddr8", Types.OTHER, "MAC address (8-byte)"),

    // === Geometric ===
    POINT("point", Types.OTHER, "2D geometric point"),
    LINE("line", Types.OTHER, "Infinite line"),
    LSEG("lseg", Types.OTHER, "Line segment"),
    BOX("box", Types.OTHER, "Rectangular box"),
    PATH("path", Types.OTHER, "Polygonal path"),
    POLYGON("polygon", Types.OTHER, "Closed polygon"),
    CIRCLE("circle", Types.OTHER, "Circular region"),

    // === Others ===
    MONEY("money", Types.OTHER, "Currency value"),
    XML("xml", Types.SQLXML, "XML content"),
    TSVECTOR("tsvector", Types.OTHER, "Full-text document vector"),
    TSQUERY("tsquery", Types.OTHER, "Full-text search query");

    companion object {
        /**
         * Maps a DDL SQL type name (e.g., "varchar") to a [PostgresDataType].
         * @param sqlTypeName the type name as written in a CREATE TABLE statement.
         */
        fun fromSqlType(sqlTypeName: String): PostgresDataType? {
            val normalized = sqlTypeName.trim().toLowerCase()
                .removeSuffix("[]") // optional: normalize array type
            return values().firstOrNull { it.sqlTypeName == normalized }
        }

        /**
         * Maps a JDBC type + PostgreSQL type name to [PostgresDataType].
         * @param jdbcType JDBC type (from java.sql.Types).
         * @param typeName the database-specific type name (e.g., "text", "jsonb").
         */
        fun fromJdbc(jdbcType: Int, typeName: String?): PostgresDataType? {
            val normalized = typeName?.trim()?.toLowerCase()
            return values().firstOrNull {
                it.jdbcType == jdbcType && (normalized == null || it.sqlTypeName == normalized)
            }
        }

        private val SYNONYMS = mapOf(
            "bool" to BOOLEAN,
            "int" to INTEGER,
            "int4" to INTEGER,
            "int2" to SMALLINT,
            "int8" to BIGINT,
            "float4" to REAL,
            "float8" to DOUBLE_PRECISION,
            "serial4" to SERIAL,
            "serial8" to BIGSERIAL,
            "decimal" to DECIMAL,
            "numeric" to NUMERIC,
            "text" to TEXT,
            "uuid" to UUID
            // Add more as needed
        )

        /**
         * Tries to map an arbitrary input (e.g., from metadata, user input, or SQL DDL)
         * to a matching [PostgresDataType].
         */
        fun fromAny(input: String): PostgresDataType? {
            val normalized = input.trim().toLowerCase().removeSuffix("[]")

            return values().firstOrNull {
                it.sqlTypeName == normalized || it.name.toLowerCase() == normalized
            } ?: SYNONYMS[normalized]
        }
    }
}
