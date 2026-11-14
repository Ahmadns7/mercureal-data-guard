package com.mercureal.dataguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mercureal.dataguard.ui.DataGuardApp
import com.mercureal.dataguard.ui.MainViewModel

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DataGuardApp(vm, onOpenUsageAccess = { startActivity(vm.usageAccessIntent()) }) }
    }

    override fun onResume() {
        super.onResume()
        vm.refresh()
    }
}
