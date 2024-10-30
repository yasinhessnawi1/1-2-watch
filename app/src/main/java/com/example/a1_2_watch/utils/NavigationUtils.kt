package com.example.a1_2_watch.utils

import android.app.Activity
import android.content.Intent
import com.example.a1_2_watch.ui.DetailsActivity
import com.example.a1_2_watch.ui.HomeActivity

object NavigationUtils {
    fun navigateToDetails(activity: Activity, mediaId: Int, mediaType: String) {
        val intent = Intent(activity, DetailsActivity::class.java).apply {
            putExtra("MEDIA_ID", mediaId)
            putExtra("MEDIA_TYPE", mediaType)
        }
        activity.startActivity(intent)
    }

    fun navigateToHome(activity: Activity) {
        val intent = Intent(activity, HomeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}