package com.example.a1_2_watch.utils

import android.app.Activity
import android.content.Intent
import com.example.a1_2_watch.ui.DetailsActivity
import com.example.a1_2_watch.ui.DiscoverActivity
import com.example.a1_2_watch.ui.HomeActivity
import com.example.a1_2_watch.ui.UserPageActivity

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

    fun navigateToUser(activity: Activity) {
        val intent = Intent(activity, UserPageActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    fun navigateToDiscover(activity: Activity) {
        val intent = Intent(activity, DiscoverActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}