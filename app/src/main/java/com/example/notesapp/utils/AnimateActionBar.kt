package com.example.notesapp.utils

import android.content.Context
import android.view.animation.AnimationUtils
import androidx.appcompat.app.ActionBar
import com.example.notesapp.R

object AnimateActionBar {
    fun animateTitleChange(actionBar: ActionBar?, context: Context, newTitle: String) {
        if(actionBar==null) return
        val oldTitle = actionBar.title.toString()
        val slideLeft = AnimationUtils.loadAnimation(context, R.anim.slide_left)
        val slideRight = AnimationUtils.loadAnimation(context, R.anim.slide_right)

        actionBar.title = oldTitle
        actionBar.title = newTitle
        actionBar.title = oldTitle
        val animOut = if (oldTitle.length > newTitle.length) slideRight else slideLeft
        actionBar.customView?.startAnimation(animOut)

        val animIn = if (oldTitle.length > newTitle.length) slideLeft else slideRight
        actionBar.title = newTitle
        actionBar.customView?.startAnimation(animIn)
    }
}