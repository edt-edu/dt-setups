package dts.modelmanager

import java.util.*
/**
 * Abstract base class for reading key-value configuration properties from a file.
 *
 * <p>This class provides a framework for loading properties from the specified file name.
 * Subclasses are expected to provide domain-specific logic for interpreting or accessing the loaded properties.</p>
 *
 * @param fileName the name or path of the properties file to load.
 *
 * @constructor initializes the reader with the given properties file.
 */
abstract class PropertiesReader(private val fileName: String) {
    /**
     * Holds the key-value pairs loaded from the specified properties file.
     */
    private val properties = Properties()
    /**
     * Initializes the [PropertiesReader] by loading properties from the provided file.
     *
     * <p>This block attempts to read the properties file specified by [fileName]
     * and loads its contents into the [properties] object.
     * Any errors during file reading will result in a runtime exception.</p>
     */
    init {
        val file = this::class.java.classLoader.getResourceAsStream(fileName)
        properties.load(file)
    }
    /**
     * Retrieves the value associated with the specified property key.
     *
     * @param key the name of the property to retrieve.
     * @return the value associated with the given key.
     * @throws NoSuchElementException if the key does not exist in the loaded properties.
     */
    fun getProperty(key: String): String = properties.getProperty(key)
}