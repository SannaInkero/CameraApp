package com.example.cameraapp

import android.app.DownloadManager
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA")
    private val REQUEST_CODE_PERMISSION = 1001

    private lateinit var cameraThread: HandlerThread
    private lateinit var cameraHandler: Handler
    private lateinit var demoCamera: DemoCamera
    private lateinit var texture : TextureView
    private lateinit var imageView: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var button = findViewById<Button>(R.id.button)
        imageView = findViewById(R.id.kuva)
        texture = findViewById(R.id.texture)


        if(allPermissionsGranted()){
            //permission ok
            Log.v("CameraApp","Permission ok")
        }
        else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION)
            Log.v("CameraApp","Ask permissions")
        }
        button.setOnClickListener() {
            demoCamera.takePhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_CODE_PERMISSION){
            if(allPermissionsGranted()){
                //Start camera
                Log.v("CameraApp","Permission ok")
            }
            else{
                Log.v("CameraApp","Permission NOT ok")
                Toast.makeText(this, "no permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun allPermissionsGranted() : Boolean{
        //Tsekkaa onko kaikki permissionit annettu
        for (permission in REQUIRED_PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

    override fun onPause(){

        demoCamera.shutDownCamera()
        cameraThread.quitSafely()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        cameraThread = HandlerThread("CameraThread")
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)
        demoCamera = DemoCamera(onImageAvailableListener, cameraHandler, texture)


        if(texture.isAvailable) {
            startReview()
        }
        else{
            texture!!.surfaceTextureListener = surfaceTextureListener
        }
    }

    private fun startReview(){
        demoCamera.openCamera(this)
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

            startReview()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }
    }


    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {reader ->
        Log.v("DemoCameraApp", "onImageAvailableListener")
        val image = reader.acquireLatestImage()
        val imageBuf = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuf.remaining())
        imageBuf.get(imageBytes)
        image.close()
        pictureReady(imageBytes)
    }


    private fun pictureReady(imageBytes : ByteArray){
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        runOnUiThread {
            imageView.setImageBitmap(bitmap)
        }
    }
}