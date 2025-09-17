package dts.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * This interface is used as a template for REST services.
 */
interface IRestService {
    object OkHttpClient {
        val instance = OkHttpClient()
    }

    /**
     * This function makes a get request to the demo Service.
     * @param command: The command for the get mapping without the /.
     */
    fun makeGetAPICall(command: String) : String? {
        val url = "http://localhost:8080/$command"
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            OkHttpClient.instance.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                return response.body!!.string()
            }
        } catch (e: Exception) {
            return null
        }

    }

    /**
     * This function makes a post request to the demo Service.
     * @param command: The command for the post mapping without the /.
     * @param json: The json that should be posted.
     */
    fun makePostAPICall(command: String, json: String) : String? {
        val url = "http://localhost:8080/$command"
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody())
            .build()
        try {
            OkHttpClient.instance.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                return response.body!!.string()
            }
        } catch (e: Exception) {
            return null
        }
    }

}