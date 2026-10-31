package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class NotificationExitPolicyTest {
    @Test fun backFinishingActivityDismissesStatus() {
        assertTrue(NotificationExitPolicy.shouldDismissOnActivityStop(
            isFinishing = true, isChangingConfigurations = false,
        ))
    }

    @Test fun homeAndRecentsOverviewPreserveLiveNotification() {
        assertFalse(NotificationExitPolicy.shouldDismissOnActivityStop(
            isFinishing = false, isChangingConfigurations = false,
        ))
    }

    @Test fun configurationChangeDoesNotCountAsUserExit() {
        assertFalse(NotificationExitPolicy.shouldDismissOnActivityStop(
            isFinishing = true, isChangingConfigurations = true,
        ))
        assertFalse(NotificationExitPolicy.shouldDismissOnActivityStop(
            isFinishing = false, isChangingConfigurations = true,
        ))
    }
}
