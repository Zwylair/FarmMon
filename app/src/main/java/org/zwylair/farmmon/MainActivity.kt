package org.zwylair.farmmon

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.zwylair.farmmon.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val updateButton = findViewById<Button>(R.id.updateButton)
        val serverAddressField = findViewById<TextInputEditText>(R.id.serverAddressInputField)

        updateButton.setOnClickListener {
            val url = serverAddressField.text.toString()

            if (checkUrl(url)) {
                FetchMoistureDataTask(url, this).run()
            } else {
                Toast.makeText(this, R.string.InvalidUrlText, Toast.LENGTH_SHORT).show()
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
        private val moistureField = activity.findViewById<TextView>(R.id.moistureInfoMoistureValue)
        private val statusField = activity.findViewById<TextView>(R.id.moistureInfoStatusValue)
        private val moistureLevelMap = mapOf(
            "1001-9999" to resources.getString(R.string.moistureLevelDisconnected),
            "601-1000" to resources.getString(R.string.moistureLevelDry),
            "371-600" to resources.getString(R.string.moistureLevelHumid),
            "0-370" to resources.getString(R.string.moistureLevelNotInSoil)
        )

        fun run() {
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                val data = fetchData()
                if (data.isEmpty) return@execute

                val analogPinValue = data.get("analogPinValue").asInt

                moistureField.post { changeMoistureValue(analogPinValue.toString()) }

                for ((range, hint) in moistureLevelMap) {
                    val (rangeStart, rangeEnd) = range.split("-").map { it.toInt() }

                    if (analogPinValue in rangeStart..rangeEnd) {
                        statusField.post { (changeStatusValue(hint)) }
                        return@execute
                    }
                }
                statusField.post { changeStatusValue(resources.getString(R.string.moistureLevelDisconnected)) }
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

                val data = response.toString()
                if (data.isEmpty()) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, R.string.emptyServerResponse, Toast.LENGTH_SHORT).show()
                    }
                    return JsonObject()
                }

                return JsonParser.parseString(data).asJsonObject
            } catch (e: Exception) {
                e.printStackTrace()
                activity.runOnUiThread {
                    Toast.makeText(activity, R.string.unableToConnectToServer, Toast.LENGTH_SHORT).show()
                }
                return JsonObject()
            }
        }

        private fun changeMoistureValue(text: String) { moistureField.text = text }
        private fun changeStatusValue(text: String) { statusField.text = text }
    }
}
