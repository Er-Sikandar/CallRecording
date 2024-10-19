package com.sikandar.callrecording

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sikandar.callrecording.FileUploadWorker.Companion.sendNotification

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val PERMISSION_REQUEST_CODE = 123
    private val permissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val permissions_33 = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request permissions
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val notGrantedPermissions = ArrayList<String>()
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions_33
        } else {
            permissions
        }
        for (permission in p) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPermissions.add(permission)
            }
        }

        if (notGrantedPermissions.isNotEmpty()) {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                notGrantedPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // All permissions granted, proceed with audio recording
            sendNotification("Permission Granted", "Permission Granted Successfully")
            startRecording()
        }
    }

    private fun startRecording() {
        val serviceIntent = Intent(this, BackgroundRecordingService::class.java)
        // Use startForegroundService for Android 8.0 (Oreo) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted, proceed with audio recording
                    startRecording()
                } else {
                    // Permission denied, inform the user and ask to enable from settings
                    showPermissionDialog()
                }
            }
        }
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissions Required")
            .setMessage("Please enable the required permissions from the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, PERMISSION_REQUEST_CODE)
    }

    fun upload(view: View) {
        val context = view.context
        if (isServiceRunning(BackgroundRecordingService::class.java)) {
            openCall("8601854014")
        } else {
            startRecording()
        }


        /**
         * Default Setting
         */
        val recordedFilesDir = context.getExternalFilesDir(null)
        if (recordedFilesDir != null && recordedFilesDir.exists()) {
            val filesList = recordedFilesDir.listFiles { file -> file.extension == "mp3" }
            if (filesList.isNullOrEmpty()) {
                Log.e("FileList", "No recorded call files found.")
            } else {
                filesList.forEach { file ->
                    Log.e("FileList", "File: ${file.name}, Path: ${file.absolutePath}")
                    /* if (file.exists()) {
                         if (file.delete()) {
                             Log.d("DeleteFile", "File deleted successfully");
                         } else {
                             // File deletion failed
                             Log.d("DeleteFile", "File deletion failed");
                         }
                     } else {
                         Log.d("DeleteFile", "File does not exist");
                     }*/
                }
            }
        } else {
            Log.e("FileList", "Directory does not exist or is empty.")
        }
        /*    val uploadWorkRequest = OneTimeWorkRequest.Builder(FileUploadWorker::class.java)
                    .setInputData(workDataOf(FileUploadWorker.KEY_FILE_PATH to filePath))
                    .build()
            context.sendNotification("File Enque", "Enque.")
            WorkManager.getInstance(context).enqueue(uploadWorkRequest)*/
    }


    fun openCall(mob: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CALL_PHONE
                )
            ) {
                showPermissionDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    111
                )
            }
        } else {
            val call_intent = Intent(Intent.ACTION_DIAL)
            call_intent.setData(Uri.parse("tel:$mob"))
            startActivity(call_intent)
        }
    }
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.getRunningServices(Int.MAX_VALUE).any {
            serviceClass.name == it.service.className
        }
    }
    /*
    private fun isServiceRunning(): Boolean {
        return BackgroundRecordingService.isRunning
    }
    */

}
