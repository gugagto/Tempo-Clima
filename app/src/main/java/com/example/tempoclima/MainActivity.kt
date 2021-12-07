package com.example.tempoclima

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tempoclima.models.WeatherResponse
import com.example.tempoclima.network.WeatherService
import com.example.tempoclima.viewModel.MainViewModel
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {


    private lateinit var mViewModel: MainViewModel
    private var mProgressDialog: Dialog?=null
    private lateinit var mFuseLocationClient: FusedLocationProviderClient
    private lateinit var mSharedPreferences: SharedPreferences
    private var latitude:Double=0.0
    private var longitude:Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mViewModel  = ViewModelProvider(this).get(MainViewModel::class.java)
        mFuseLocationClient= LocationServices.getFusedLocationProviderClient(this)

        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME,Context.MODE_PRIVATE)




        if (!isLocationOn())
        {
            Toast.makeText(this,"Location provider is off! Please check!",Toast.LENGTH_LONG).show()
        }else
        {
            showProgressDialog()
            requestLocationData()
        }



        observe()
        mViewModel.isNetworkAvailable()


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        if (item.itemId==R.id.action_refresh)
        {
            mViewModel.isNetworkAvailable()
        }


    }

   private fun observe()
   {
        mViewModel.network.observe(this, Observer {
            if (it)
            {
                getWeatherDetails(latitude,longitude)
            }
            else
            {
                dismissDialog()
                if (mSharedPreferences!=null){

                  val data =  mSharedPreferences.getString("weather","")
                  val weather = Gson().fromJson(data,WeatherResponse::class.java)
                  setupUi(weather)
                  Snackbar.make(ll_main,"No internet connection!",Snackbar.LENGTH_INDEFINITE)
                      .setActionTextColor(Color.WHITE)
                      .setTextColor(Color.RED)
                      .setAction("OK", View.OnClickListener {
                          dismissDialog()
                      })
                      .show()



              }
            }
        })

       mViewModel.retrofitInstance.observe(this, Observer {


           val weather = it
           val weatherJson= Gson().toJson(weather)
           val editor = mSharedPreferences.edit()
           editor.putString("weather",weatherJson)
           editor.apply()

           setupUi(weather)
           Log.e("weather","$weather")


       })

   }



    private fun getWeatherDetails( latitude :Double, longitude:Double) {

        dismissDialog()
       mViewModel.getRetrofit(latitude,longitude)

    }


    private fun setupUi(response: WeatherResponse) {

        for (i in response.weather.indices)
        {

            tv_main.text= response.weather[i].main
            tv_main_description.text  =response.weather[i].description
            tv_temp.text= response.main.temp.toString() + " °C"
            tv_country.text= response.sys.country
            tv_sunrise_time.text = unixtime(response.sys.sunrise)
            tv_sunset_time.text = unixtime(response.sys.sunset)
            tv_name.text= response.name
            tv_min.text = response.main.temp_min.toString() + " min"
            tv_max.text =response.main.temp_max.toString() + " max"
            tv_speed.text= response.wind.speed.toString()
            tv_humidity.text= response.main.humidity.toString() + "% Humidity"

            when(response.weather[i].icon)
            {
                "01d" -> iv_main.setImageResource(R.drawable.sunny)
                "02d" -> iv_main.setImageResource(R.drawable.cloud)
                "03d" -> iv_main.setImageResource(R.drawable.cloud)
                "04d" -> iv_main.setImageResource(R.drawable.cloud)
                "04n" -> iv_main.setImageResource(R.drawable.cloud)
                "10d" -> iv_main.setImageResource(R.drawable.rain)
                "11d" -> iv_main.setImageResource(R.drawable.storm)
                "13d" -> iv_main.setImageResource(R.drawable.snowflake)
                "01n" -> iv_main.setImageResource(R.drawable.cloud)
                "02n" -> iv_main.setImageResource(R.drawable.cloud)
                "03n" -> iv_main.setImageResource(R.drawable.cloud)
                "10n" -> iv_main.setImageResource(R.drawable.cloud)
                "11n" -> iv_main.setImageResource(R.drawable.rain)
                "13n" -> iv_main.setImageResource(R.drawable.snowflake)


            }


        }

    }

    private fun  unixtime(timex:Long):String
    {
        val date= Date(timex *1000L)
        val sdf= SimpleDateFormat("HH:mm")
        sdf.timeZone= TimeZone.getDefault()
        return  sdf.format(date)
    }




    private fun showProgressDialog()
    {
        mProgressDialog = Dialog(this)
       mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()

    }

    private fun dismissDialog()
    {
        if (mProgressDialog!=null)
        {
            mProgressDialog!!.dismiss()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocation= LocationRequest()
        mLocation.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
        mFuseLocationClient.requestLocationUpdates(mLocation,mLocationCallback, Looper.myLooper())

    }

    private val mLocationCallback= object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val mLastLocation: Location = p0.lastLocation
            latitude= mLastLocation.latitude
            longitude= mLastLocation.longitude
            Log.e("lat", "$latitude" + "\n" + "$longitude")
            getWeatherDetails(latitude,longitude)


        }
    }

    private fun isLocationOn():Boolean{

        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
       return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)




    }



}