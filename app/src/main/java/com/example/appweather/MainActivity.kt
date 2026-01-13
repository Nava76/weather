package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var cityInput: EditText
    private lateinit var weatherResult: TextView
    private val apiKey = "f2a06777b8ac29b8087bd69a4ddaf415"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFlipper = findViewById(R.id.viewFlipper)
        cityInput = findViewById(R.id.cityInput)
        weatherResult = findViewById(R.id.weatherResult)

        // Page 1: Check Weather
        findViewById<Button>(R.id.checkWeatherBtn).setOnClickListener {
            viewFlipper.showNext()
        }
        findViewById<Button>(R.id.backFromCheckBtn).setOnClickListener {
            finish()
        }

        // Page 2: Enter City
        findViewById<Button>(R.id.getWeatherBtn).setOnClickListener {
            val city = cityInput.text.toString().trim()
            if (city.isNotEmpty()) {
                getWeather(city)
                viewFlipper.showNext()
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.backFromEnterBtn).setOnClickListener {
            viewFlipper.showPrevious()
        }

        // Page 3: Result/Error
        findViewById<Button>(R.id.backFromResultBtn).setOnClickListener {
            viewFlipper.displayedChild = 1 // Go back to Enter City page
        }
    }

    private fun getWeather(city: String) {
        val client = OkHttpClient()
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    weatherResult.text = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    try {
                        val jsonObject = JSONObject(jsonData!!)
                        val cityName = jsonObject.getString("name")
                        val temp = jsonObject.getJSONObject("main").getDouble("temp")
                        val condition = jsonObject.getJSONArray("weather")
                            .getJSONObject(0).getString("description")

                        val result = "$cityName\nTemperature: $temp °C\nCondition: $condition"

                        runOnUiThread {
                            weatherResult.text = result
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            weatherResult.text = "Parsing error"
                        }
                    }
                } else {
                    runOnUiThread {
                        weatherResult.text = "City not found or API error"
                    }
                }
            }
        })
    }
}