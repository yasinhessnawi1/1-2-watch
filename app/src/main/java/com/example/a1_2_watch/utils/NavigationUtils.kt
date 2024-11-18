package com.example.a1_2_watch.utils

import android.app.Activity
import android.content.Intent
import com.example.a1_2_watch.ui.DetailsActivity
import com.example.a1_2_watch.ui.DiscoverActivity
import com.example.a1_2_watch.ui.HomeActivity
import com.example.a1_2_watch.ui.UserLikedActivity

/**
 * NavigationUtils provides a utility object for navigation functionality for the main activities
 * in the app. This includes many methods to navigate to a specific page and passing relevant
 * data if needed.
 *
 */
object NavigationUtils {

    /**
     * This method navigates to the DetailsActivity and displaying details for a specific media item.
     * this method accepts the ID and type of the media item, which are used in the details activity to
     * fetch and display relevant information.
     *
     * @param activity The current activity context.
     * @param mediaId The ID of the media item to be displayed (movie, show, or anime).
     * @param mediaType The type of media item to determine which data and layout would be
     *                  be displayed.
     */
    fun navigateToDetails(activity: Activity, mediaId: Int, mediaType: String) {
        // Create the intent to start DetailsActivity, and passing the media ID and type as extras.
        val intent = Intent(activity, DetailsActivity::class.java).apply {
            // Add the media ID and type to the intent extras.
            putExtra("MEDIA_ID", mediaId)
            putExtra("MEDIA_TYPE", mediaType)
        }
        // Start DetailsActivity with our created intent.
        activity.startActivity(intent)
    }

    /**
     * This method navigates to the HomeActivity, which is the main activity for our app.
     *
     * @param activity The current activity context.
     */
    fun navigateToHome(activity: Activity) {
        // Create intent to start the home (main) activity.
        val intent = Intent(activity, HomeActivity::class.java)
        // Start HomeActivity with our created intent.
        activity.startActivity(intent)
    }

    /**
     * This method navigates to the UerPageActivity, which displays the user favorites media
     * list like the favorites movie, TvShow, and anime.
     *
     * @param activity The current activity context.
     */
    fun navigateToUser(activity: Activity) {
        // Create intent to navigate to User Page activity
        val intent = Intent(activity, UserLikedActivity::class.java)
        // Start UserPageActivity with our created intent.
        activity.startActivity(intent)
    }

    /**
     * This method navigates to the DiscoverActivity, which displays list of related media content,
     * if the user add any media to the favorites list in the user page, the related media will be
     * shown in the discover activity.
     *
     * @param activity The current activity context.
     */
    fun navigateToDiscover(activity: Activity) {
        // Create intent to navigate to the Discover Activity.
        val intent = Intent(activity, DiscoverActivity::class.java)
        // Start DiscoverActivity with our created intent.
        activity.startActivity(intent)
    }
}