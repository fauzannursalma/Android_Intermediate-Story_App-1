package com.dicoding.fauzann.storyapp.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dicoding.fauzann.storyapp.databinding.ActivityDetailStoryBinding
import com.dicoding.fauzann.storyapp.model.Story

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var activityDetailStoryBinding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityDetailStoryBinding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(activityDetailStoryBinding.root)

        supportActionBar?.hide()

        val story = intent.getParcelableExtra<Story>(DETAIL_STORY) as Story
        Glide.with(this)
            .load(story.photo)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(activityDetailStoryBinding.imgPhotoDetail)
        activityDetailStoryBinding.tvNameDetail.text = story.name
        activityDetailStoryBinding.tvDescriptionDetail.text = story.description

        playAnimation()

    }

    private fun playAnimation() {

        val nameTV = ObjectAnimator.ofFloat(activityDetailStoryBinding.tvNameDetail, View.ALPHA, 1f).setDuration(500)
        val descTV = ObjectAnimator.ofFloat(activityDetailStoryBinding.tvDescriptionDetail, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(nameTV, descTV)
            start()
        }
    }

    companion object {
        const val DETAIL_STORY = "detail_story"
    }
}