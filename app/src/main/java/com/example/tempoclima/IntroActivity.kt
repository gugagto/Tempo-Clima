package com.example.tempoclima

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class IntroActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        window.decorView.apply {

            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }


        Handler().postDelayed({


         chkPermissions()

        },3000)

    }

    private fun chkPermissions() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE).withListener(object :MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                if (report!!.areAllPermissionsGranted())
                {
                    startActivity(Intent(this@IntroActivity,MainActivity::class.java))
                    finish()
                }


            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {

                AlertDialog.Builder(this@IntroActivity).setMessage("Permission denied.Please change").setPositiveButton("Settings"){
                        _,_ ->
                    try {
                        val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri= Uri.fromParts("package",packageName,null)
                        intent.data=uri
                        startActivity(intent)
                    }catch (e : ActivityNotFoundException){

                        e.printStackTrace()
                    }
                }.setNegativeButton("Cancel"){ dialog, which ->
                    dialog.dismiss()
                }.show()


            }

        }).onSameThread().check()

    }
}