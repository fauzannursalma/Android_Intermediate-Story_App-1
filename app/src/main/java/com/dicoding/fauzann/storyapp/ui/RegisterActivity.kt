package com.dicoding.fauzann.storyapp.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.dicoding.fauzann.storyapp.R
import com.dicoding.fauzann.storyapp.api.ApiConfig
import com.dicoding.fauzann.storyapp.databinding.ActivityRegisterBinding
import com.dicoding.fauzann.storyapp.response.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var activityRegisterBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(activityRegisterBinding.root)

        supportActionBar?.hide()

        playAnimation()

        activityRegisterBinding.etNameRegister.type = "name"
        activityRegisterBinding.etEmailRegister.type = "email"
        activityRegisterBinding.etPasswordRegister.type = "password"

        activityRegisterBinding.btnRegister.setOnClickListener {
            val inputName = activityRegisterBinding.etNameRegister.text.toString()
            val inputEmail = activityRegisterBinding.etEmailRegister.text.toString()
            val inputPassword = activityRegisterBinding.etPasswordRegister.text.toString()

            createAccount(inputName, inputEmail, inputPassword)
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

    private fun createAccount(inputName: String, inputEmail: String, inputPassword: String) {
        showLoading(true)

        val client = ApiConfig.getApiService().createAccount(inputName, inputEmail, inputPassword)
        client.enqueue(object: Callback<RegisterResponse>{
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                showLoading(false)
                val responseBody = response.body()
                Log.d(TAG, "onResponse: $responseBody")
                if(response.isSuccessful && responseBody?.message == "User created") {
                    Toast.makeText(this@RegisterActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.e(TAG, "onFailure1: ${response.message()}")
                    Toast.makeText(this@RegisterActivity, getString(R.string.register_fail), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "onFailure2: ${t.message}")
                Toast.makeText(this@RegisterActivity, getString(R.string.register_fail), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            activityRegisterBinding.progressBar.visibility = View.VISIBLE
        } else {
            activityRegisterBinding.progressBar.visibility = View.GONE
        }
    }

    private fun playAnimation() {

        val registerTV = ObjectAnimator.ofFloat(activityRegisterBinding.tvTitleRegister, View.ALPHA, 1f).setDuration(500)
        val nameEdt = ObjectAnimator.ofFloat(activityRegisterBinding.etNameRegister, View.ALPHA, 1f).setDuration(500)
        val emailEdt = ObjectAnimator.ofFloat(activityRegisterBinding.etEmailRegister, View.ALPHA, 1f).setDuration(500)
        val passwordEdt = ObjectAnimator.ofFloat(activityRegisterBinding.etPasswordRegister, View.ALPHA, 1f).setDuration(500)
        val registerBtn = ObjectAnimator.ofFloat(activityRegisterBinding.btnRegister, View.ALPHA, 1f).setDuration(500)


        AnimatorSet().apply {
            playSequentially(registerTV, nameEdt, emailEdt, passwordEdt, registerBtn)
            start()
        }
    }

    companion object {
        private const val TAG = "Register Activity"
    }

}