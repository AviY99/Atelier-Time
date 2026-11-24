package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class D161PauseWarningTrackerTest {
    @Test fun strictThresholdAndSoundUntilDismissal() {
        val policy = PauseWarningTracker()
        assertFalse(policy.observe(true, false, 10_000L).showing)
        assertFalse(policy.observe(true, false, 70_000L).showing)
        val overdue = policy.observe(true, false, 70_001L)
        assertTrue(overdue.classWarning)
        assertFalse(overdue.freeWarning)
        assertTrue(policy.observe(true, false, 190_000L).showing)
        policy.dismissVisible(overdue)
        assertFalse(policy.observe(true, false, 190_010L).showing)
        assertFalse(policy.observe(true, false, 1_000_000L).showing)
        policy.observe(false, false, 1_000_001L)
        assertFalse(policy.observe(true, false, 1_001_000L).showing)
        assertTrue(policy.observe(true, false, 1_061_001L).showing)
    }

    @Test fun twoPausedTimersRemainIndependentAndSilenceTogetherOnClose() {
        val policy = PauseWarningTracker()
        policy.observe(true, false, 0L)
        policy.observe(true, true, 45_000L)
        val onlyClass = policy.observe(true, true, 61_001L)
        assertTrue(onlyClass.classWarning)
        assertFalse(onlyClass.freeWarning)
        policy.dismissVisible(onlyClass)
        assertFalse(policy.observe(true, true, 80_000L).showing)
        val onlyFree = policy.observe(true, true, 105_001L)
        assertFalse(onlyFree.classWarning)
        assertTrue(onlyFree.freeWarning)
        policy.dismissVisible(onlyFree)
        assertFalse(policy.observe(true, true, 200_000L).showing)
        assertFalse(policy.observe(false, false, 200_001L).showing)
    }
}
