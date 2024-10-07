package site.riddleling.tpctrashcanmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }
    val context = LocalContext.current

    val mainViewModel: MainViewModel = koinViewModel()
    val trashCanData by mainViewModel.trashCanData.collectAsStateWithLifecycle()
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val placeData by mainViewModel.placeData.collectAsStateWithLifecycle()

    var openSheet = remember { mutableStateOf(false) }
    var selectedTrashCan by remember { mutableStateOf<TrashCanData?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    // 請求權限
    LaunchedEffect(Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 獲取當前位置
    if (hasLocationPermission) {
        LaunchedEffect(Unit) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    currentLocation = location
                }
        }
    }

    // 設置地圖位置
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(25.046856, 121.516923), // 預設位置（台北車站）
            18f
        )
    }

    // 獲取到當前位置後，更新地圖位置
    currentLocation?.let { location ->
        LaunchedEffect(location) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    18f
                )
            )
        }
    }

    // 獲取到地點資料後，更新地圖位置
    placeData?.let { place ->
        LaunchedEffect(placeData) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(place.latitude, place.longitude),
                    17f
                )
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = hasLocationPermission,
                //zoomControlsEnabled = false
            ),
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            val icon = bitmapDescriptor(context, R.drawable.trash_pin_s)
            val icon2 = bitmapDescriptor(context, R.drawable.map_pin_s)
            trashCanData.forEach { trashCan ->
                Marker(
                    state = MarkerState(LatLng(trashCan.latitude, trashCan.longitude)),
                    title = trashCan.address,
                    icon = icon,
                    draggable = false,
                    onClick = {
                        selectedTrashCan = trashCan
                        openSheet.value = true
                        false
                    }
                )
            }

            if (placeData != null) {
                Marker(
                    state = MarkerState(LatLng(placeData!!.latitude, placeData!!.longitude)),
                    title = placeData!!.title,
                    snippet = placeData!!.subtitle,
                    icon = icon2,
                    draggable = false,
                    onClick = {
                        false
                    }
                )
            }
        }

        if (isLoading) {
            DownloadProgressIndicator(
                Modifier.align(Alignment.Center)
            )
        }

        if (openSheet.value) {
            NavigateModalBottomSheet(selectedTrashCan, openSheet)
        }
    }
}

fun bitmapDescriptor(context: Context, vectorResId: Int): BitmapDescriptor? {
    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    // draw it onto the bitmap
    val canvas = android.graphics.Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}

@Composable
@Preview
fun HomeScreenContentPreview() {
    HomeScreenContent()
}