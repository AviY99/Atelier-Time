package com.ateliertime.android

import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Side-effect-free device QA. Never changes the saved Class library, audio
 * settings, notifications, permissions, or active app process.
 */
@RunWith(AndroidJUnit4::class)
class RunEndTimeDeviceTest {
    private fun m(n: Int) = 60 * n
    private fun session() = ScheduleEngine().let { engine ->
        engine.approve(engine.generate(SessionRules(
            id = "device-finish", name = "Device finish validation",
            totalSeconds = m(180), summarySeconds = m(15), poseSeconds = m(25),
            specialBreakCount = 1,
        ), FinalPoseChoice.remainder()))
    }

    @Test fun exactPlannedFinishIsValidOnDevice() {
        val s = session()
        val now = System.currentTimeMillis()
        val target = RunEndTimePlanner.suggestedEndEpochMs(s, now)
        val p = RunEndTimePlanner.plan(s, now, target)
        assertEquals(0, p.correctionSeconds)
        assertEquals(s.rules.totalSeconds, p.adjustedDurationSeconds)
    }

    @Test fun shortAndLongSelectedFinishesPreserveSavedApproval() {
        val s = session()
        val saved = s.phases.toList()
        val now = System.currentTimeMillis()
        val before = RunEndTimePlanner.plan(s, now, now + m(177) * 1_000L)
        val after = RunEndTimePlanner.plan(s, now, now + m(186) * 1_000L)
        assertEquals(m(177), before.adjusted.totalSeconds)
        assertEquals(m(186), after.adjusted.totalSeconds)
        assertEquals(saved, s.phases)
    }

    @Test fun impossibleFinishIsRejectedBeforeTimerStarts() {
        val s = session()
        var now = SystemClock.elapsedRealtime()
        val timer = MonotonicSessionTimer(s) { now }
        val chosen = System.currentTimeMillis()
        try {
            RunEndTimePlanner.plan(s, chosen, chosen + m(150) * 1_000L)
            fail("Impossible early finish must be rejected")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.READY, timer.view().status)
    }

    @Test fun monotonicDeadlineSurvivesDisplayedClockShift() {
        val s = session()
        var now = SystemClock.elapsedRealtime()
        val timer = MonotonicSessionTimer(s) { now }
        val started = timer.start(m(3))
        val tracker = ClassClockTracker { now }
        val epoch = System.currentTimeMillis()
        val tracked = tracker.newClass(started, epoch)
        now += m(45) * 1_000L
        val viewed = tracker.observe(timer.update().view, epoch - m(120) * 1_000L)
        assertEquals(m(45), viewed.classElapsedSeconds)
        assertEquals(m(132), viewed.classRemainingSeconds)
        assertEquals(tracked.classEndEpochMs, viewed.classEndEpochMs)
    }

    @Test fun finishIsNeverBeforeOneMinutePerLastSession() {
        val s = session()
        val now = System.currentTimeMillis()
        val lastFeasible = RunEndTimePlanner.plan(s, now, now + m(152) * 1_000L)
        assertEquals(m(1), lastFeasible.adjusted.firstFinalPoseSeconds)
        assertEquals(m(1), lastFeasible.adjusted.lastFinalPoseSeconds)
    }

    @Test fun mainActivityAndForegroundServiceHaveExpectedExportSettings() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
        )
        val entry = info.activities?.firstOrNull { it.name == MainActivity::class.java.name }
        val foreground = info.services?.firstOrNull {
            it.name == ClassNotificationTaskService::class.java.name
        }
        assertNotNull("MainActivity must be declared", entry)
        assertTrue("Launcher Activity should be exported", entry!!.exported)
        assertNotNull("Foreground service must be declared", foreground)
        assertFalse("Notification service must not be exported", foreground!!.exported)
    }

    @Test fun rejectedClockTextDoesNotChangeLiveTimer() {
        val s = session()
        val timer = MonotonicSessionTimer(s) { SystemClock.elapsedRealtime() }
        try {
            RunEndTimePlanner.nextOccurrence("25:19", System.currentTimeMillis())
            fail("Invalid hours should fail")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.READY, timer.view().status)
    }

    @Test fun devicePauseAfterPenultimateSessionOnlyShortensLastUnstartedOne() {
        val session = session()
        val regular = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val breakBetween = session.phases.first {
            it.startSeconds == regular.first().endSeconds
        }
        var now = SystemClock.elapsedRealtime()
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now += (breakBetween.startSeconds + 2L) * 1000L
        timer.pause()
        now += 20_000L
        val resumed = timer.resume()
        assertEquals(TimerStatus.RUNNING, resumed.status)
        assertEquals(regular.first().durationSeconds,
            timer.finalTwoPoseDurations()?.first)
        assertEquals(regular.last().durationSeconds - 20,
            timer.finalTwoPoseDurations()?.second)
    }

    @Test fun deviceRejectsLateEndBeyondTwiceOriginalClass() {
        val session = session()
        val original = session.phases.toList()
        val now = System.currentTimeMillis()
        try {
            RunEndTimePlanner.plan(session, now, now + m(361).toLong() * 1_000L)
            fail("Overlong finish should be rejected.")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("too late"))
        }
        assertEquals(original, session.phases)
    }

    @Test fun rejectedFinishDoesNotLaunchClockOrEmitMelodyCue() {
        val session = session()
        var now = SystemClock.elapsedRealtime()
        val timer = MonotonicSessionTimer(session) { now }
        try {
            RunEndTimePlanner.plan(session, 0L, m(151) * 1000L)
            fail("Impossible early time should not start the Class")
        } catch (_: IllegalArgumentException) { }
        now += 10_000L
        val event = timer.update()
        assertEquals(TimerStatus.READY, event.view.status)
        assertNull(event.cue)
        assertEquals(session.rules.totalSeconds, event.view.totalSeconds)
    }

    @Test fun expiredMonotonicPhysicalDeadlineFinishesPausedClassExactlyOnce() {
        val session = session()
        var now = SystemClock.elapsedRealtime()
        val timer = MonotonicSessionTimer(session) { now }
        val tracker = ClassClockTracker { now }
        val epoch = System.currentTimeMillis()
        tracker.newClass(timer.start(-m(6)), epoch, epoch + m(186) * 1000L)
        now += 30_000L
        timer.pause()
        now += m(186).toLong() * 1000L
        assertTrue(tracker.hasReachedClassDeadline(epoch))
        assertEquals(TimerCue.SESSION_FINISHED, timer.finishAtDeadline().cue)
        assertNull(timer.finishAtDeadline().cue)
    }

}
