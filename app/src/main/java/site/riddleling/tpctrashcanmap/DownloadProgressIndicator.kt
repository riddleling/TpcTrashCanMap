package site.riddleling.tpctrashcanmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DownloadProgressIndicator(modifier: Modifier = Modifier) {
    Column (
        modifier = modifier
            .size(width = 200.dp, height = 100.dp)
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

@Composable
@Preview
fun DownloadProgressIndicatorPreview() {
    DownloadProgressIndicator()
}