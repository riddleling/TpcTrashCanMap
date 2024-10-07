package site.riddleling.tpctrashcanmap

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainViewModel : ViewModel() {
    private val infoUrl = "https://wlmaplab.github.io/json/tpc-trash-dataset.json"
    private val fetchLimit = 1000
    private var fetchOffset = 0
    private var dataUrl = ""
    private var dataList: MutableList<TrashCanData> = mutableListOf<TrashCanData>()

    private val _trashCanData : MutableStateFlow<MutableList<TrashCanData>> = MutableStateFlow(mutableListOf<TrashCanData>())
    val trashCanData: StateFlow<MutableList<TrashCanData>> = _trashCanData

    private val _isLoading : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _placeData : MutableStateFlow<PlaceData?> = MutableStateFlow(null)
    val placeData: StateFlow<PlaceData?> = _placeData


    init {
        fetchData()
    }

    fun setPlaceData(placeData: PlaceData?) {
        _placeData.value = placeData
    }

    fun fetchData() {
        _trashCanData.value = mutableListOf<TrashCanData>()
        _isLoading.value = true
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(infoUrl).build()

        CoroutineScope(Dispatchers.IO).launch {
            var response = client.newCall(request).execute()
            response.body?.run {
                val json = JSONObject(string())
                dataUrl = json.getString("url")
                dataList.clear()
                fetchOffset = 0
                downloadTrashCanData()
            }
        }
    }

    private suspend fun downloadTrashCanData() {
        val dataUrl = "$dataUrl&limit=$fetchLimit&offset=$fetchOffset"
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(dataUrl).build()

        var response = client.newCall(request).execute()
        response.body?.run {
            val json = JSONObject(string())
            val results = json.getJSONObject("result").getJSONArray("results")
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                try {
                    val latitude = item.getString("緯度").toDouble()
                    val longitude = item.getString("經度").toDouble()
                    val address = item.getString("地址")
                    val data = TrashCanData(latitude, longitude, address)
                    dataList.add(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (results.length() >= fetchLimit) {
                fetchOffset += fetchLimit
                downloadTrashCanData()
            } else {
                println("dataList: ${dataList.size}")
                _isLoading.value = false
                _trashCanData.value = dataList
            }
        }
    }
}