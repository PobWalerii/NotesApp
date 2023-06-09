package com.example.notesapp.constants

object KeyConstants {
    const val DATABASE_NAME = "data-notes"
    const val REMOTE_DATABASE_NAME = "remote-data-notes"
    const val TIME_DELAY_START: Int = 5
    const val TIME_DELAY_QUERY: Int = 1
    const val INTERVAL_REQUESTS: Int = 1
    const val TIME_DELAY_OPERATION: Int = 0
    const val DEFAULT_HEADER = "Note"
    const val DEFAULT_SPECIFICATION_LINE = true
    const val DEFAULT_ADD_IF_CLICK = true
    const val DELETE_IF_SWIPED = true
    const val DATE_CHANGE_WHEN_CONTENT = true
    const val SHOW_MESSAGE_INTERNET_OK = false
    const val SING_OF_FIRST_RUN = true
    const val CREATE_RECORDS_IN_BACKGROUND = false
    const val INTERVAL_BACKGROUND_CREATE: Int = 60
    const val MIN_INTERVAL_BACKGROUND_CREATE: Int = 5

    const val CHANNEL_ID = "BackService"
    const val NOTIFICATION_ID = 11111
    const val CHANNEL_IDD = "BackRemoteService"
    const val NOTIFICATION_IDD = 22222

    const val MAX_RETRY_ATTEMPTS = 3
    const val MIN_DELAY_FOR_REMOTE = 500L

    const val AF_DEV_KEY = "jCum3K5NFNe9aqByMTV2oe"
    const val AF_EVENT_CLICK = "item_click"
    const val AF_EVENT_NEW = "item_new"
    const val AF_EVENT_DELETE = "item_delete"
}