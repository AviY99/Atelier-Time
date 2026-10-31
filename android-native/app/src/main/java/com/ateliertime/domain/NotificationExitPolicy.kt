package com.ateliertime.domain

/**
 * Android navigation policy for the optional ongoing Class status.
 *
 * Home, Recents overview and normal backgrounding must not dismiss it.
 * A true Activity finish (Back), except a configuration recreation, must.
 * Recents task removal is handled separately by Service.onTaskRemoved.
 */
object NotificationExitPolicy {
    fun shouldDismissOnActivityStop(
        isFinishing: Boolean,
        isChangingConfigurations: Boolean,
    ): Boolean = isFinishing && !isChangingConfigurations
}
