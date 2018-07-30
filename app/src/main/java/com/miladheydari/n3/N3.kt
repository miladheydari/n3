package com.miladheydari.n3

import android.app.Application
import ir.tapsell.sdk.Tapsell
import ir.tapsell.sdk.TapsellConfiguration


class N3 : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = TapsellConfiguration(this)
        config.permissionHandlerMode = TapsellConfiguration.PERMISSION_HANDLER_DISABLED
        Tapsell.initialize(this@N3, config, BuildConfig.tapsellToken)
    }
}