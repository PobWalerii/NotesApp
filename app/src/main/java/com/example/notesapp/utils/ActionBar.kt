package com.example.notesapp.utils

import android.app.Activity
import android.content.Context
import android.provider.Settings.Global.getString
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.notesapp.R
import com.example.notesapp.ui.main.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppActionBar(
    private val activity: Activity,
    private val context: Context,
    private val titleId: Int,
    private val lifecycleOwner: LifecycleOwner,
    private val isHomeKey: Boolean = true,
    private val isSave: Boolean = false,
    private val isDelete: Boolean = false,
    private val isSettings: Boolean = false,
    private val toDefault: Boolean = false
) {

    private val isItemMenuPressed = MutableStateFlow("")
    val isItemMenuPressedFlow: StateFlow<String> = isItemMenuPressed.asStateFlow()

    private var appbarMenu: Menu? = null
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
                menu.findItem(R.id.todefault).isVisible = toDefault
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.appbar_menu, menu)
                appbarMenu = menu
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
                    R.id.todefault -> {
                        itemMenuPressed("todefault")
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
        appbarMenu?.findItem(
            when (item) {
                "save" -> R.id.save
                "delete" -> R.id.delete
                "settings" -> R.id.settings
                "todefault" -> R.id.todefault
                else -> 0
            }
        )?.isVisible = isVisible
        isItemMenuPressed.value = ""
    }

    fun startCounter(seconds: Int) {
        if (seconds > 1) {
            var count = seconds
            CoroutineScope(Dispatchers.Main).launch {
                while (count >= 0) {
                    setSpannableTitle(
                        if (count > 0) {
                            context.getString(R.string.text_wait) + " $count"
                        } else ""
                    )
                    count--
                    withContext(Dispatchers.Default) { delay(1000) }
                }
            }
        }
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