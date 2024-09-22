package site.riddleling.tpctrashcanmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import site.riddleling.tpctrashcanmap.ui.theme.TpcTrashCanMapTheme
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TpcTrashCanMapTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainActivityContent(Modifier.padding(it))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(modifier: Modifier = Modifier) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }
    val context = LocalContext.current

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

    val mainViewModel: MainViewModel = viewModel()
    val trashCanData by mainViewModel.trashCanData.collectAsStateWithLifecycle()
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    var openSheet by remember { mutableStateOf(false) }
    var selectedTrashCan by remember { mutableStateOf<TrashCanData?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = hasLocationPermission,
                zoomControlsEnabled = false
            ),
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            val icon = bitmapDescriptor(context, R.drawable.trash_pin_s)
            trashCanData.forEach { trashCan ->
                Marker(
                    state = MarkerState(LatLng(trashCan.latitude, trashCan.longitude)),
                    title = trashCan.address,
                    icon = icon,
                    draggable = false,
                    onClick = {
                        selectedTrashCan = trashCan
                        openSheet = true
                        false
                    }
                )
            }
        }

        if (isLoading) {
            Column (
                modifier = Modifier
                    .size(width = 200.dp, height = 100.dp)
                    .align(Alignment.Center)
                    .background(
                        Color.LightGray.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = "下載資料中...")
            }
        }

        if (openSheet) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { openSheet = false }
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    IconButton(
                        onClick = { openSheet = false },
                        modifier = Modifier
                            .align(Alignment.End)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.close_24dp),
                            contentDescription = "close",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedTrashCan?.address ?: "",
                            Modifier.fillMaxWidth(0.7f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                val activity = context as? Activity
                                navigateToLocation(activity!!,
                                    selectedTrashCan?.latitude!!,
                                    selectedTrashCan?.longitude!!)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF019858))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.navigation_24dp),
                                contentDescription = "Icon",
                                modifier = Modifier.size(16.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "導航", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

}

fun navigateToLocation(activity: Activity, latitude: Double, longitude: Double) {
    val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=w")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    if (mapIntent.resolveActivity(activity.packageManager) != null) {
        activity.startActivity(mapIntent)
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
fun MainActivityContentPreview() {
    MainActivityContent()
}