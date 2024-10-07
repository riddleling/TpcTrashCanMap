package site.riddleling.tpctrashcanmap

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.riddleling.tpctrashcanmap.ui.theme.TpcTrashCanMapTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TpcTrashCanMapTheme {

                Scaffold(
                    topBar = { HomeTopBar() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeScreenContent(Modifier.padding(it))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    val mainViewModel: MainViewModel = koinViewModel()
    val context = LocalContext.current

    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        actions = {
            IconButton(
                onClick = {
                    mainViewModel.fetchData()
                },
                content = {
                    Image(
                        painter = painterResource(id = R.drawable.refresh_24dp),
                        contentDescription = "refresh",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            IconButton(
                onClick = {
                    //navController.navigate(MainUnitScreen.route_search)

                    val intent = Intent(context, SearchActivity::class.java)
                    context.startActivity(intent)
                },
                content = {
                    Image(
                        painter = painterResource(id = R.drawable.search_24dp),
                        contentDescription = "search",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    )
}