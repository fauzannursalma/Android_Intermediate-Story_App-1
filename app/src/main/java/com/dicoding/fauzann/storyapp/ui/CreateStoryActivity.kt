package com.dicoding.fauzann.storyapp.ui

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.fauzann.storyapp.*
import com.dicoding.fauzann.storyapp.api.ApiConfig
import com.dicoding.fauzann.storyapp.databinding.ActivityCreateStoryBinding
import com.dicoding.fauzann.storyapp.response.FileUploadResponse
import com.dicoding.fauzann.storyapp.utils.UserPreference
import com.dicoding.fauzann.storyapp.utils.reduceFileImage
import com.dicoding.fauzann.storyapp.utils.rotateBitmap
import com.dicoding.fauzann.storyapp.utils.uriToFile
import com.dicoding.fauzann.storyapp.viewmodel.SharedViewModel
import com.dicoding.fauzann.storyapp.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class CreateStoryActivity : AppCompatActivity() {
    private lateinit var createStoryViewModel: SharedViewModel
    private lateinit var createStoryBinding: ActivityCreateStoryBinding
    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createStoryBinding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(createStoryBinding.root)

        setupViewModel()

        supportActionBar?.hide()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        playAnimation()

        createStoryBinding.btnCamera.setOnClickListener {
            startCamera()
        }

        createStoryBinding.btnGallery.setOnClickListener {
            startGallery()
        }

        createStoryBinding.btnUpload.setOnClickListener {
            uploadImage()
        }
    }

    private fun setupViewModel() {
        createStoryViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[SharedViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val addMenu = menu.findItem(R.id.menu_add)
        addMenu.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
                return true
            }

            R.id.menu_logout -> {
                createStoryViewModel.logout()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadImage() {
        showLoading(true)

        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val description = createStoryBinding.etDescription.text.toString()
                .toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            createStoryViewModel.getUser().observe(this) {
                if (it != null) {
                    val client = ApiConfig.getApiService()
                        .uploadImage("Bearer " + it.token, imageMultipart, description)
                    client.enqueue(object : Callback<FileUploadResponse> {
                        override fun onResponse(
                            call: Call<FileUploadResponse>,
                            response: Response<FileUploadResponse>
                        ) {
                            showLoading(false)
                            val responseBody = response.body()
                            Log.d(TAG, "onResponse: $responseBody")
                            if (response.isSuccessful && responseBody?.message == "Story created successfully") {
                                Toast.makeText(
                                    this@CreateStoryActivity,
                                    getString(R.string.upload_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this@CreateStoryActivity, StoryActivity::class.java)
                                startActivity(intent)
                            } else {
                                Log.e(TAG, "onFailure1: ${response.message()}")
                                Toast.makeText(
                                    this@CreateStoryActivity,
                                    getString(R.string.upload_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                            showLoading(false)
                            Log.e(TAG, "onFailure2: ${t.message}")
                            Toast.makeText(
                                this@CreateStoryActivity,
                                getString(R.string.upload_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }

        } else {
            showLoading(false)
            Toast.makeText(
                this@CreateStoryActivity,
                getString(R.string.upload_warning),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            getFile = myFile

            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )
            createStoryBinding.imgPreview.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@CreateStoryActivity)
            getFile = myFile
            createStoryBinding.imgPreview.setImageURI(selectedImg)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            createStoryBinding.progressBar.visibility = View.VISIBLE
        } else {
            createStoryBinding.progressBar.visibility = View.GONE
        }
    }

    private fun playAnimation() {

        val preIV = ObjectAnimator.ofFloat(createStoryBinding.imgPreview, View.ALPHA, 1f).setDuration(500)
        val camBtn = ObjectAnimator.ofFloat(createStoryBinding.btnCamera, View.ALPHA, 1f).setDuration(500)
        val galBtn = ObjectAnimator.ofFloat(createStoryBinding.btnGallery, View.ALPHA, 1f).setDuration(500)
        val descEdt = ObjectAnimator.ofFloat(createStoryBinding.etDescription, View.ALPHA, 1f).setDuration(500)
        val uploadBtn = ObjectAnimator.ofFloat(createStoryBinding.btnUpload, View.ALPHA, 1f).setDuration(500)


        AnimatorSet().apply {
            playSequentially(preIV, camBtn, galBtn, descEdt, uploadBtn)
            start()
        }

        ObjectAnimator.ofFloat(createStoryBinding.imgPreview, View.TRANSLATION_X, -20f, 20f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

    companion object {
        const val TAG = "CreateStoryActivity"
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}