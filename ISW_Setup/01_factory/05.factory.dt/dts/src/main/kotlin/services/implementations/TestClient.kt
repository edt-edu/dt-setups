package dts.services.implementations

import java.io.BufferedReader
import java.io.InputStreamReader

import java.io.PrintWriter
import java.net.Socket

fun main() {
    val client = TestClient()
    client.startConnection("127.0.0.1", 9999)
    var response: String? = client.sendMessage("")
    print(response)
    client.stopConnection()

    client.startConnection("127.0.0.1", 9999)
    response = client.sendMessage("1 2 multi")
    print(response)
    client.stopConnection()
    client.startConnection("127.0.0.1", 9999)
    response = client.sendMessage("1 2 multi")
    print(response)
    client.stopConnection()
    client.startConnection("127.0.0.1", 9999)
    response = client.sendMessage("1 2 multi")
    print(response)
    client.stopConnection()
    client.startConnection("127.0.0.1", 9999)
    response = client.sendMessage("EXIT")
    print(response)
    client.stopConnection()
}
class TestClient {
    private var clientSocket: Socket? = null
    private var out: PrintWriter? = null
    private var `in`: BufferedReader? = null

    fun startConnection(ip: String, port: Int) {
        clientSocket = Socket(ip, port)
        out = PrintWriter(clientSocket!!.getOutputStream(), true)
        `in` = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
    }

    fun sendMessage(msg: String?): String? {
        out!!.println(msg)
        return `in`!!.readLine()
    }

    fun stopConnection() {
        `in`!!.close()
        out!!.close()
        clientSocket?.close()
    }

}