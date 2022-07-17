package com.dicoding.fauzann.storyapp.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.fauzann.storyapp.*
import com.dicoding.fauzann.storyapp.api.ApiConfig
import com.dicoding.fauzann.storyapp.databinding.ActivityMainBinding
import com.dicoding.fauzann.storyapp.model.UserAuth
import com.dicoding.fauzann.storyapp.response.LoginResponse
import com.dicoding.fauzann.storyapp.utils.UserPreference
import com.dicoding.fauzann.storyapp.viewmodel.SharedViewModel
import com.dicoding.fauzann.storyapp.viewmodel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: SharedViewModel
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(1500)
        installSplashScreen()

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setupViewModel()

        supportActionBar?.hide()

        playAnimation()

        activityMainBinding.ivChangeLanguage.setOnClickListener {
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(intent)
        }

        activityMainBinding.etEmailLogin.type = "email"
        activityMainBinding.etPasswordLogin.type = "password"

        activityMainBinding.btnLogin.setOnClickListener {
            val inputEmail = activityMainBinding.etEmailLogin.text.toString()
            val inputPassword = activityMainBinding.etPasswordLogin.text.toString()

            login(inputEmail, inputPassword)
        }

        activityMainBinding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[SharedViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if(user.isLogin) {
                val intent = Intent(this, StoryActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val addMenu = menu.findItem(R.id.menu_add)
        val logoutMenu = menu.findItem(R.id.menu_logout)

        addMenu.isVisible = false
        logoutMenu.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
                return true
            }
        }
        return true
    }

    private fun login(inputEmail: String, inputPassword: String) {
        showLoading(true)

        val client = ApiConfig.getApiService().login(inputEmail, inputPassword)
        client.enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)
                val responseBody = response.body()
                Log.d(TAG, "onResponse: $responseBody")
                if(response.isSuccessful && responseBody?.message == "success") {
                    mainViewModel.saveUser(UserAuth(responseBody.loginResult.token, true))
                    Toast.makeText(this@MainActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, StoryActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.e(TAG, "onFailure1: ${response.message()}")
                    Toast.makeText(this@MainActivity, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "onFailure2: ${t.message}")
                Toast.makeText(this@MainActivity, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            activityMainBinding.progressBar.visibility = View.VISIBLE
        } else {
            activityMainBinding.progressBar.visibility = View.GONE
        }
    }

    private fun playAnimation() {

        val languageIV = ObjectAnimator.ofFloat(activityMainBinding.ivChangeLanguage, View.ALPHA, 1f).setDuration(500)
        val logoIV = ObjectAnimator.ofFloat(activityMainBinding.ivLogo, View.ALPHA, 1f).setDuration(500)
        val emailEdt = ObjectAnimator.ofFloat(activityMainBinding.etEmailLogin, View.ALPHA, 1f).setDuration(500)
        val passwordEdt = ObjectAnimator.ofFloat(activityMainBinding.etPasswordLogin, View.ALPHA, 1f).setDuration(500)
        val loginBtn = ObjectAnimator.ofFloat(activityMainBinding.btnLogin, View.ALPHA, 1f).setDuration(500)
        val questionTV = ObjectAnimator.ofFloat(activityMainBinding.tvAccountQuestion, View.ALPHA, 1f).setDuration(500)
        val registerBtn = ObjectAnimator.ofFloat(activityMainBinding.btnRegister, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(languageIV, logoIV, emailEdt, passwordEdt, loginBtn, questionTV, registerBtn)
            start()
        }

        ObjectAnimator.ofFloat(activityMainBinding.ivLogo, View.TRANSLATION_X, -20f, 20f).apply {
            duration = 5000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

    companion object {
        private const val TAG = "Main Activity"
    }


}