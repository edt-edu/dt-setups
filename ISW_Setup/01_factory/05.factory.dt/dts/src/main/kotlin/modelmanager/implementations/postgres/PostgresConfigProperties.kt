package dts.modelmanager.implementations.postgres

import dts.modelmanager.PropertiesReader
/**
 * Loads and provides access to PostgreSQL configuration properties.
 *
 * <p>This class extends [PropertiesReader] and is specialized for reading
 * PostgreSQL-related configuration such as connection URL, username, and password
 * from a given properties file.</p>
 *
 * <p>Typical usage involves passing a file path to the constructor,
 * which will then be used to initialize property access.</p>
 *
 * @param filepath the path to the properties file containing PostgreSQL configuration settings.
 *
 * @constructor creates an instance of [PostgresConfigProperties] with the provided file path.
 */
class PostgresConfigProperties(filepath:String): PropertiesReader(filepath)