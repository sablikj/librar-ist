package pt.ulisboa.tecnico.cmov.librarist.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.net.ConnectivityManagerCompat

fun checkNetworkType(context: Context): Pair<Boolean, Boolean> {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val isWiFi: Boolean
    val isMetered: Boolean

    // For Android 10+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val network = connectivityManager.activeNetwork ?: return Pair(false, false)
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return Pair(false, false)
        isWiFi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        isMetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED).not()
    } else {
        // For older versions of android
        @Suppress("DEPRECATION")
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        isWiFi = activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        isMetered = ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager)
    }

    return Pair(isWiFi, isMetered)
}