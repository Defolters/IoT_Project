package io.github.defolters.homeapp.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


class WeatherRecord : Serializable {
    var Temperature: Double = .0
    var CO2: Double = .0
    var Humidity: Double = .0
    var Time: String = ""
    var timestamp: Long = 0

    constructor() {}

    constructor(Temperature: Double, CO2: Double, Humidity: Double, Time: String) {
        this.Temperature = Temperature
        this.CO2 = CO2
        this.Humidity = Humidity
        this.Time = Time
    }

    override fun toString(): String {
        return "WeatherRecord $Temperature $CO2 $Humidity $Time"
    }
}

class Frequency : Serializable {
    var DelayS: Int = 0

    constructor() {}
    constructor(DelayS: Int) {
        this.DelayS = DelayS

    }

    override fun toString(): String {
        return "Frequency $DelayS"
    }
}

class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    companion object {
        const val TAG = "HomeApp"
    }

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    fun changeFrequency(seconds: Int) {
        Log.d(TAG, "sec $seconds")

        val database = Firebase.database
        val userSettingsRef = database.getReference("homeweather-iot").child("UserSettings")

        userSettingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                Log.d(TAG, "data" + p0.toString())

                Log.d(TAG, "data" + p0.children.first().key.toString())

                p0.children.first().key?.let {
                    userSettingsRef.child(it).setValue(Frequency(seconds))
                        .addOnSuccessListener {
                            Log.d(TAG, "SUCCESS SETTING")
                        }.addOnCanceledListener {

                        }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "onCancelled")
            }
        })
    }

    fun changeNotification(boolean: Boolean) {
        if (boolean) {
            FirebaseMessaging.getInstance().subscribeToTopic("homeapp")
                .addOnCompleteListener { task ->
                    var msg = "Subscribed!"
                    if (!task.isSuccessful) {
                        msg = "Not subscribed!"
                    }
                    Log.d("TAG", msg)
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("homeapp")
                .addOnCompleteListener { task ->
                    var msg = "Unsubscribed!"
                    if (!task.isSuccessful) {
                        msg = "Unsubscribed error!"
                    }
                    Log.d("TAG", msg)
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun getData(context: Context?, func: (WeatherRecord?) -> Unit) = uiScope.launch {
        val database = Firebase.database
        val myRef = database.getReference("homeweather-iot")
        myRef.child("WeatherRecords").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val weatherRecords = arrayListOf<WeatherRecord>()
                dataSnapshot.children.forEach {

                    val value = it.getValue(WeatherRecord::class.java)
//                    Log.d(TAG, "Value is: ${value.toString()}")

                    val formatter = SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",//.ssssss",
                        Locale.getDefault()
                    ) // 2018-06-19T22:38:28+0000
                    formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"))//gmt
                    val date = formatter.parse(value?.Time?.split(".")?.get(0))

                    value?.timestamp = date?.time ?: 0

                    if ((value != null) && (value.Temperature != 0.0) && (value.Humidity < 100)) {
                        weatherRecords.add(value)
                    }
                }

                // sort by date
                weatherRecords.sortBy { it.timestamp }

                // round to 2
                weatherRecords.map {
                    it.Temperature = it.Temperature.roundTo(2)
                    it.Humidity = it.Humidity.roundTo(2)
                }


                val weatherMap = weatherRecords.groupBy {
                    //get date
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))//"UTC"))
                    cal.timeInMillis = it.timestamp
                    // get current day
                    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                    //group by day
                    dayOfYear
                }

                val timestampOv = arrayListOf<Long>() // time
                val tempsOv = arrayListOf<Float>() // temperatures
                val humidsOv = arrayListOf<Float>() // humids
                val co2sOv = arrayListOf<Int>() // co2s

                weatherMap.forEach { i, list ->
                    //get for overview
                    val dayWeatherOv = list.maxBy { it.Temperature }

                    dayWeatherOv?.let {
                        timestampOv.add(it.timestamp)
                        tempsOv.add(it.Temperature.toFloat())
                        humidsOv.add(it.Humidity.toFloat())
                        co2sOv.add(it.CO2.toInt())
                    }

                    // save each day here
                    // get date and month
                    val firstWeather = list.first()
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))//"UTC"))
                    cal.timeInMillis = firstWeather.timestamp
                    // get list
                    val timestampDay = list.map { it.timestamp }
                    val tempDay = list.map { it.Temperature.toFloat() }
                    val humDay = list.map { it.Humidity.toFloat() }
                    val co2Day = list.map { it.CO2.toInt() }
                    // get json
                    val dayJsonTemp = getJson(timestampDay, tempDay, "Temp")
                    val dayJsonHum = getJson(timestampDay, humDay, "Hum")
                    val dayJsonCo2 = getJson(timestampDay, co2Day, "Co2")

                    //save to folder
                    Log.d(TAG, "")
                    saveToFolder(context, dayJsonTemp, 1, cal.timeInMillis)
                    saveToFolder(context, dayJsonHum, 2, cal.timeInMillis)
                    saveToFolder(context, dayJsonCo2, 3, cal.timeInMillis)//year, month, day)

                }
                //save overview here
                val tempOverview = getJson(timestampOv, tempsOv, "Температура")
                val humOverview = getJson(timestampOv, humidsOv, "Влажность")
                val co2Overview = getJson(timestampOv, co2sOv, "CO2")

                saveToFolderOverview(context, tempOverview, 1)
                saveToFolderOverview(context, humOverview, 2)
                saveToFolderOverview(context, co2Overview, 3)


                var last: WeatherRecord? = null // last record to show
                weatherRecords.forEach {
                    last = it
                }

                func(last)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())

                func(null)
            }
        })

    }

    fun saveToFolderOverview(context: Context?, json: String, type: Int) {
        // save to folder
        //data/name_id/overview.json
        //data/1/overview.json

        context?.let {
            val filename = "data/$type/"
//            Log.d(TAG, "file list" + it.fileList().joinToString())
            val rootDir = File(it.filesDir, filename)
            rootDir.mkdirs()

            Log.d(TAG, "file list" + rootDir.listFiles().joinToString())
            val overviewFile = File(rootDir, "overview.json")

            FileOutputStream(overviewFile).use {
                it.write(json.toByteArray())
            }

            Log.d(TAG, "saved overview $type")
        }
    }

    fun saveToFolder(
        context: Context?,
        json: String,
        type: Int,
        date: Long
    ) {
        // year:Int,month:Int,day:Int){
        //data/name_id/2020-03/01.json
        context?.let {
            val dateFormat =
                SimpleDateFormat("yyyy-MM", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("Europe/Moscow")//"UTC")

            val dayFileFormat =
                SimpleDateFormat("dd'.json'", Locale.US)
            dayFileFormat.timeZone = TimeZone.getTimeZone("Europe/Moscow")//"UTC")

            val filename = "data/$type/${dateFormat.format(date)}/"
//            Log.d(TAG, "file list" + it.fileList().joinToString())
            val rootDir = File(it.filesDir, filename)
            rootDir.mkdirs()

            Log.d(TAG, "file list" + rootDir.listFiles().joinToString())
            val overviewFile = File(rootDir, "${dayFileFormat.format(date)}")

//            Log.d(TAG, "text:"+overviewFile.readText())

            FileOutputStream(overviewFile).use {
                it.write(json.toByteArray())
            }


//            Log.d(TAG, "file list after" + it.fileList().joinToString())
            Log.d(TAG, "saved day $type $filename")
        }
    }

    fun getJson(timestamp: List<Long>, data: List<Any>, name: String): String {
        // create objects
        // convert to json
        // overview
        val jsonObject =
            json {
                "columns" To arrayWithArrays(
                    listOf(
                        arrayWithStrings(
                            listOf(
                                listOf("x"),
                                timestamp
                            ).flatten<Any>()
                        ),
                        arrayWithStrings(
                            listOf(
                                listOf("y0"),
                                data
                            ).flatten<Any>()
                        )
                    )
                ) // [["x", timestamp],["y0", temp]]
                "types" To json {
                    "y0" To "line"
                    "x" To "x"
                }
                "names" To json {
                    "y0" To name
                }
                "colors" To json {
                    "y0" To "#4BD964"
                }
            }
        Log.d(TAG, jsonObject.toString())

        return jsonObject.toString()
    }

    fun json(builderFunc: JsonObjectBuilder.() -> Unit): JSONObject {
        val builder = JsonObjectBuilder()
        builder.builderFunc()
        return builder.json
    }

    fun array(objects: List<JSONObject>): JSONArray {
        val json = JSONArray()
        objects.forEachIndexed { index, t ->
            json.put(index, t)
        }
        return json
    }

    fun arrayWithStrings(objects: List<Any>): JSONArray {
        val json = JSONArray()
        objects.forEachIndexed { index, t ->
            json.put(index, t)
        }
        return json
    }

    fun arrayWithArrays(objects: List<JSONArray>): JSONArray {
        val json = JSONArray()
        objects.forEachIndexed { index, t ->
            json.put(index, t)
        }
        return json
    }

    class JsonObjectBuilder {
        val json = JSONObject()

        infix fun <T> String.To(value: T) {
            json.put(this, value)
        }
    }
}
