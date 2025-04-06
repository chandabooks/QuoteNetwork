package com.example.quotenetwork

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotenetwork.ui.theme.QuoteNetworkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuoteNetworkTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val vmm = QuoteVMM()
                    QuotationsGUI(
                        vmm, modifier = Modifier
                            .fillMaxSize()
                            .padding(56.dp)
                    )
                }

            }
        }
    }
}

@Composable
fun QuotationsGUI(vmm: QuoteVMM, modifier: Modifier = Modifier) {
    val uiState by vmm.uiState.collectAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuotationCard(uiState, modifier = Modifier.fillMaxSize(0.9f))
        Button(onClick = { vmm.newQuote() }) {
            Text(text = "Inspire Me")
        }
    }
}



@Composable
fun QuotationCard(quote: Quotation, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = quote.quote,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(56.dp))
            Text(
                text = quote.person,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )

        }
    }
}

data class Quotation(val person: String, val quote: String)

private const val BASE_URL = "http://10.0.2.2:8000/"

class QuoteVMM : ViewModel() {

    private val defValue = Quotation("System", "Nothing to Show yet")
    private var _uiState = MutableStateFlow(defValue)
    val uiState: StateFlow<Quotation> = _uiState.asStateFlow()

    init {
        newQuote()
    }

    fun newQuote() {
        viewModelScope.launch {
            try {
                val quoteString = QuoteAPI.retrofitService.getQuote()
                val quote = JSONObject(quoteString)
                val value = Quotation(quote.getString("Person"), quote.getString("Quote"))
                _uiState.value = value
            } catch (e: Exception) {
                Log.d("MY_TAG", e.localizedMessage?:"An Exception Happened")
            }
        }
    }

}

private val retrofit =
    Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl(BASE_URL)
        .build()

interface QuoteAPIService {
    @GET("/")
    suspend fun getQuote(): String
}

object QuoteAPI {
    val retrofitService: QuoteAPIService by lazy {
        retrofit.create(QuoteAPIService::class.java)
    }
}