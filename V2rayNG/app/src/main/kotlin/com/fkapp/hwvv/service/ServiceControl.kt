package com.fkapp.hwvv.service

import android.app.Service

interface ServiceControl {
    fun getService(): Service

    fun startService(parameters: String)

    fun stopService()

    fun vpnProtect(socket: Int): Boolean

}
