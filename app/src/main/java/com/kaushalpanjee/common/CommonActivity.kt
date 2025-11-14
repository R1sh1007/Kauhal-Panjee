package com.kaushalpanjee.common

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.kaushalpanjee.R
import com.kaushalpanjee.core.basecomponent.BaseActivity
import com.kaushalpanjee.core.util.AppUtil
import com.kaushalpanjee.databinding.ActivityCommonBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommonActivity : BaseActivity<ActivityCommonBinding>(ActivityCommonBinding::inflate) {

    private var navController: NavController? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navGraphHost) as NavHostFragment
        navController = navHostFragment.navController
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_graph)
        val isLoggedIn = AppUtil.getLoginStatus(this)
        if (isLoggedIn) {
            navGraph.setStartDestination(R.id.mainHomePage)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }

        navController?.graph = navGraph
    }
}
