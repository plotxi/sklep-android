package com.example.sklep.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

object NetworkUtils {
    fun hasInternet(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun friendlyMessage(error: Throwable): String {
        return when (error) {
            is SocketTimeoutException, is TimeoutException -> "Serwer nie odpowiada. Spróbuj ponownie za chwilę."
            is UnknownHostException -> "Brak połączenia z internetem."
            else -> "Błąd połączenia. Spróbuj ponownie."
        }
    }
}
