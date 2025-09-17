package dts.dtsystem.mappingutils

import java.io.File
import java.nio.file.Paths

class MappingConsolidation {

    // returns the first value of the list of inputs if no consolidation function is given
    fun consolidate(scriptPath: String, input: MutableList<Pair<String, Any?>>): Any? {
        // pass the input to a function according to script path
        val file = File(Paths.get("").toAbsolutePath().toString(), scriptPath)
        if (file.exists()) {
            //TODO not yet implemented}
            return input.get(0).second
        }
        return input.get(0).second

        // if the answer is none we just pass back a unifying function already available
    }
}