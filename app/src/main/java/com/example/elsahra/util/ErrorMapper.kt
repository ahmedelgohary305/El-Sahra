package com.example.elsahra.util

import android.content.Context
import com.example.elsahra.R
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {
    fun mapThrowableToMessage(context: Context, throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> context.getString(R.string.error_network)
            is SocketTimeoutException -> context.getString(R.string.error_timeout)
            is HttpException -> context.getString(R.string.error_server)
            else -> context.getString(R.string.error_unknown)
        }
    }

    /**
     * Non-context version that returns string resource IDs
     */
    fun mapThrowableToStringRes(throwable: Throwable): Int {
        return when (throwable) {
            is UnknownHostException -> R.string.error_network
            is SocketTimeoutException -> R.string.error_timeout
            is HttpException -> R.string.error_server
            else -> R.string.error_unknown
        }
    }
}
