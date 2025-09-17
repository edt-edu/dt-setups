package dts.utils

import java.util.logging.Logger

class DTLogger() {
    companion object {
        lateinit var LOG: Logger;
        fun info(className: String?, content: String) {
            LOG = Logger.getLogger(className)
            LOG.info("[$className]$content")
        }

        fun warn(className: String?, content: String) {
            LOG = Logger.getLogger(className)
            LOG.warning("[$className]$content")
        }

        fun error(className: String?, content: String) {
            LOG = Logger.getLogger(className)
            LOG.severe("[$className]$content")
        }

        fun debug(className: String?, content: String) {
            LOG = Logger.getLogger(className)
            LOG.fine("[$className]$content")
        }
    }
}