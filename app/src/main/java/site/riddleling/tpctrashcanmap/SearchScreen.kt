package site.riddleling.tpctrashcanmap

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    var predictions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }
    val textFieldValue = rememberSaveable { mutableStateOf("") }
    val mainViewModel: MainViewModel = koinViewModel()
    val activity = getActivity()

    Column (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = textFieldValue.value,
            label = { Text("搜尋地點或地址") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            onValueChange = {
                textFieldValue.value = it
                if (it.length > 2) {
                    val token = AutocompleteSessionToken.newInstance()
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(it)
                        .build()

                    try {
                        placesClient.findAutocompletePredictions(request).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val response = task.result
                                predictions = response?.autocompletePredictions ?: emptyList()
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                println("Error: ${task.exception?.message}")
                            }
                        }
                    } catch (e: Exception) {
                        predictions = emptyList()
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        println("Error: ${e.message}")
                    }
                } else {
                    predictions = emptyList()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        predictions.forEach { prediction ->
            Text(
                text = "${prediction.getPrimaryText(null).toString()}, ${prediction.getSecondaryText(null).toString()}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val placeFields = listOf(Place.Field.LOCATION)
                        val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)

                        placesClient.fetchPlace(request).addOnSuccessListener { response ->
                            val place = response.place
                            val location: LatLng? = place.location

                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude
                                val placeData = PlaceData(
                                    latitude, longitude,
                                    prediction.getPrimaryText(null).toString(),
                                    prediction.getSecondaryText(null).toString())
                                mainViewModel.setPlaceData(placeData)
                                mainViewModel.setIsMoveToPlace(true)
                                activity?.finish()
                            }
                        }.addOnFailureListener { exception ->
                            println("Place not found: ${exception.message}")
                        }
                    }
                    .padding(8.dp)
            )
            HorizontalDivider()
        }
    }
}

@Composable
@Preview
fun SearchScreenContentPreview() {
    SearchScreenContent()
}
