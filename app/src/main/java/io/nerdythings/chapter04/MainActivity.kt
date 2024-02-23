package io.nerdythings.chapter04

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


class MainActivity : ComponentActivity() {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addOkHttpLoggingInterceptor(this)
                    addInterceptor(OkHttpProfilerInterceptor())
                }
            }
            .build()
    }

    private fun addOkHttpLoggingInterceptor(builder: OkHttpClient.Builder) {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        builder.addInterceptor(interceptor)
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val service: GitHubService by lazy {
        retrofit.create(GitHubService::class.java)
    }

    interface GitHubService {
        @GET(JSON_URL)
        suspend fun listColors(): Map<String, List<Int>>
    }

    private val lastRequestTime = MutableSharedFlow<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val lastTime by lastRequestTime.collectAsState(initial = 0)
            OnButtonScreen(lastTime)
        }
    }

    private fun makeHttpRequest() = lifecycleScope.launch(Dispatchers.IO) {
        val time = SystemClock.uptimeMillis()
        service.listColors()
        val resultTime = SystemClock.uptimeMillis() - time
        lastRequestTime.emit(resultTime)
    }

    @Composable
    fun OnButtonScreen(lastTime: Long) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (lastTime > 0) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    text = "The latest request took $lastTime ms",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
                    .align(Alignment.Center),
                onClick = {
                    makeHttpRequest()
                },
            ) {
                Text("Make an HTTP request!")
            }

            FloatingActionButton(
                onClick = {
                    openNerdyThings()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Image(
                    painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "Favorite"
                )
            }
        }
    }

    private fun openNerdyThings() {
        val url = "https://www.youtube.com/@Nerdy.Things"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        startActivity(intent)
    }

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com"
        private const val JSON_URL =
            "$BASE_URL/Nerdy-Things/04-android-network-inspector/master/colors.json"
    }
}
