package com.example.notesapp.ui.MyActionBar

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.notesapp.R
import com.example.notesapp.data.repository.NotesRepository
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.ui.main.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyActionBar (
    private val notesRepository: NotesRepository,
    private val appSettings: AppSettings,
    private val context: Context,
 ){

    private val isItemMenuPressed = MutableStateFlow("")
    val isItemMenuPressedFlow: StateFlow<String> = isItemMenuPressed.asStateFlow()

    private var appbarMenu: Menu? = null
    private var actionBar: ActionBar? = null
    private var title = ""
    private var counter: Job? = null
    lateinit var activity: Activity

    init {
        CoroutineScope(Dispatchers.Default).launch {
            notesRepository.counterDelayFlow.collect {
                startCounter(it)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            notesRepository.isLoadFlow.collect {
                isLoadProcess(it)
            }
        }
    }


    fun init(
        _activity: Activity,
        titleId: Int,
        lifecycleOwner: LifecycleOwner,
        isHomeKey: Boolean = true,
        isSave: Boolean = false,
        isDelete: Boolean = false,
        isSettings: Boolean = false,
        toDefault: Boolean = false,
    ) {
        activity = _activity

        actionBar = (activity as MainActivity).supportActionBar
        title = context.getString(titleId)

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
                when (menuItem.itemId) {
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
        if (item == "home") {
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

    fun startCounter(start: Boolean) {
        if (start) {
            var count = 1
            counter = CoroutineScope(Dispatchers.Main).launch {
                while (start) {
                    setSpannableTitle(context.getString(R.string.text_wait) + " $count")
                    count++
                    withContext(Dispatchers.Default) { delay(1000) }
                }
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                counter?.cancel()
                setSpannableTitle("")
            }
        }
    }

    fun isLoadProcess(isLoad: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            if (isLoad) {
                counter?.cancel()
            }
            setSpannableTitle(if (isLoad) context.getString(R.string.text_load) else "")
        }
    }

    private fun setSpannableTitle(text: String) {
        val spannableString = SpannableString(title + text)
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray)),
            title.length,
            title.length + text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        actionBar?.title = spannableString
    }

}
