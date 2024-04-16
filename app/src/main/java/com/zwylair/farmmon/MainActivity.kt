package com.zwylair.farmmon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
//import android.widget.Toast
//import com.fazecast.jSerialComm.SerialPort
import com.zwylair.farmmon.databinding.ActivityMainBinding
//import java.io.InputStreamReader
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
//    private lateinit var serialPort: SerialPort

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val reader = InputStreamReader(serialPort.inputStream)
//        val buffer = CharArray(1024)
//        var readBytes: Int
//
//        serialPort = SerialPort.getCommPort("COM6")
//        serialPort.baudRate = 57600
//        serialPort.openPort()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val updateButton = findViewById<Button>(R.id.update_button)
        val moistureValue = findViewById<TextView>(R.id.moisture_value1)

        updateButton.setOnClickListener {
//            readBytes = reader.read(buffer)
//            moistureValue.text = String(buffer, 0, readBytes)
            moistureValue.text = Random.nextInt(0, 1000).toString()

//            serialPort.closePort()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.  ## ActionBar
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}