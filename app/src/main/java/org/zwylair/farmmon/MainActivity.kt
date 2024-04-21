package org.zwylair.farmmon

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import org.zwylair.farmmon.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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
        val moistureValue = findViewById<TextView>(R.id.moistureValue1)
        val serverAddressField = findViewById<TextInputEditText>(R.id.serverAddressInputField)

        updateButton.setOnClickListener {
            val url = serverAddressField.text.toString()

            if (checkUrl(url)) {
                FetchMoistureDataTask(moistureValue, url).execute()
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
        private val textView: TextView,
        private val url: String
    ) {
        fun execute() {
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                val result = doInBackground()
                textView.post { onPostExecute(result) }
            }
        }

        private fun doInBackground(): String {
            try {
                val connection = URL("http://$url:5000").openConnection() as HttpURLConnection
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
                connection.disconnect()

                return response.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                return "Error: ${e.message}"
            }
        }

        private fun onPostExecute(result: String?) {
            result?.let { textView.text = it }
        }
    }
}
