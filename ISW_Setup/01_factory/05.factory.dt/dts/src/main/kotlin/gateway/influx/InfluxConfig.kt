package dts.gateway.influx

data class InfluxConfig(
    val url: String,
    val token: String,
    val org: String,
    val bucket: String
)
