package com.fkapp.hwvv.ui

import com.fkapp.hwvv.R
import com.fkapp.hwvv.util.Utils
import android.os.Bundle
import com.fkapp.hwvv.service.V2RayServiceManager

class ScSwitchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveTaskToBack(true)

        setContentView(R.layout.activity_none)

        if (V2RayServiceManager.v2rayPoint.isRunning) {
            Utils.stopVService(this)
        } else {
            Utils.startVServiceFromToggle(this)
        }
        finish()
    }
}
