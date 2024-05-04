package org.zwylair.farmmon

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

import org.zwylair.farmmon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.updateButton.setOnClickListener {
            val url = binding.serverAddressInput.text.toString()

            if (checkUrl(url)) {
                FetchMoistureDataTask(url, this).run()
            } else {
                Toast.makeText(this, R.string.invalid_url_text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUrl(url: String): Boolean {
        val urlParts = url.split(".")

        if (urlParts.size != 4) return false
        return !((urlParts[3].toIntOrNull() ?: -1) > 255 || (urlParts[3].toIntOrNull() ?: -1) < 0)
    }

    class FetchMoistureDataTask(
        private val url: String,
        private val activity: AppCompatActivity
    ) {
        private val resources = activity.resources
        private val moistureField = activity.findViewById<TextView>(R.id.moisture_value)
        private val statusField = activity.findViewById<TextView>(R.id.status_value)
        private val moistureLevelMap = mapOf(
            "1001-9999" to resources.getString(R.string.moisture_level_disconnected),
            "601-1000" to resources.getString(R.string.moisture_level_dry),
            "371-600" to resources.getString(R.string.moisture_level_humid),
            "0-370" to resources.getString(R.string.moisture_level_not_in_soil)
        )

        fun run() {
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                val data = fetchData()
                if (data.isEmpty) return@execute

                val analogPinValue = data.get("analogPinValue").asInt

                for ((range, hint) in moistureLevelMap) {
                    val (rangeStart, rangeEnd) = range.split("-").map { it.toInt() }

                    if (analogPinValue in rangeStart..rangeEnd) {
                        activity.runOnUiThread {
                            moistureField.text = analogPinValue.toString()
                            statusField.text = hint
                        }
                        break
                    }
                }
            }
        }

        private fun fetchData(): JsonObject {
            try {
                val connection = URL("http://$url:8099").openConnection() as HttpURLConnection
                connection.connectTimeout = 1500
                connection.readTimeout = 1500
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
                connection.disconnect()

                return try {
                    JsonParser.parseString(response.toString()).asJsonObject
                } catch (e: JsonSyntaxException) {
                    showToast(R.string.failed_to_process_data)
                    JsonObject()
                }
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                showToast(R.string.request_timed_out)
                return JsonObject()
            } catch (e: ConnectException) {
                e.printStackTrace()
                showToast(R.string.unable_to_connect_to_server)
                return JsonObject()
            }
        }

        private fun showToast(textKey: Int) {
            activity.runOnUiThread { Toast.makeText(activity, textKey, Toast.LENGTH_SHORT).show() }
        }
    }
}
