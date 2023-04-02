package com.example.notesapp.utils

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.notesapp.R
import com.example.notesapp.ui.main.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppActionBar(
    private val activity: Activity,
    private val context: Context,
    titleId: Int,
    lifecycleOwner: LifecycleOwner,
    isHomeKey: Boolean = true,
    isSave: Boolean = false,
    isDelete: Boolean = false,
    isSettings: Boolean = false,
) {

    private val isItemMenuPressed = MutableStateFlow("")
    val isItemMenuPressedFlow: StateFlow<String> = isItemMenuPressed.asStateFlow()

    lateinit var appbarMenu: Menu
    private val actionBar = (activity as MainActivity).supportActionBar
    private val title = context.getString(titleId)

    init {
        actionBar?.title = title
        actionBar?.setDisplayHomeAsUpEnabled(isHomeKey)
        (activity as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.save).isVisible = isSave
                menu.findItem(R.id.delete).isVisible = isDelete
                menu.findItem(R.id.settings).isVisible = isSettings
                appbarMenu = menu
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.appbar_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    android.R.id.home -> {
                        itemMenuPressed("home")
                    }
                    R.id.save -> {
                        itemMenuPressed("save")
                    }
                    R.id.delete -> {
                        itemMenuPressed("delete")
                    }
                    R.id.settings -> {
                        itemMenuPressed("settings")
                    }
                    else -> {}
                }
                return true
            }
        }, lifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun itemMenuPressed(item: String) {
        if(item=="home") {
            (activity as MainActivity).onSupportNavigateUp()
        } else {
            isItemMenuPressed.value = item
            isItemMenuPressed.value = ""
        }
    }

    fun setButtonVisible(item: String, isVisible: Boolean) {
        appbarMenu.findItem(
            when (item) {
                "save" -> R.id.save
                "delete" -> R.id.delete
                "settings" -> R.id.settings
                else -> 0
            }
        ).isVisible = isVisible
        isItemMenuPressed.value = ""
    }

    fun setSpannableTitle(text: String) {
        val spannableString = SpannableString(title+text)
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray)),
            title.length,
            title.length+text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        actionBar?.title = spannableString
    }

}