package com.example.tempoclima.viewModel

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.location.LocationManager
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tempoclima.Constants
import com.example.tempoclima.models.WeatherResponse
import com.example.tempoclima.network.WeatherService
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel(application: Application):AndroidViewModel(application) {




        private val mNetwork = MutableLiveData<Boolean>()
        var network: LiveData<Boolean> = mNetwork


        private val mRetrofit = MutableLiveData<WeatherResponse>()
        var retrofitInstance: LiveData<WeatherResponse> = mRetrofit

        fun isNetworkAvailable()
        {
                mNetwork.value = Constants.isNetworkAvailable(getApplication())



        }

        fun getRetrofit(latitude:Double,longitude:Double)
        {

                val retrofit : Retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val service: WeatherService = retrofit.create(WeatherService::class.java)
                    val call : Call<WeatherResponse> = service.getWeather(latitude,longitude,Constants.METRIC_UNIT,Constants.APP_ID)
                    call.enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(
                            call: Call<WeatherResponse>,
                            response: Response<WeatherResponse>
                        ) {
                            if (response.isSuccessful)
                            {
                               mRetrofit.value= response.body()

                            }
                        }

                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            Log.e("Error","${t.message}")
                        }


                    })



        }











}