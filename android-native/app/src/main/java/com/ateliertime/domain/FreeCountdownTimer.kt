package com.ateliertime.domain

/** Independent, monotonic HH:MM:SS countdown; never touches a saved Class. */
enum class FreeTimerStatus { READY, RUNNING, PAUSED, FINISHED }

data class FreeTimerView(
    val status: FreeTimerStatus,
    val durationSeconds: Int,
    val remainingSeconds: Int,
)

data class FreeTimerTick(val view: FreeTimerView, val completedNow: Boolean = false)

class FreeCountdownTimer(private val nowMillis: () -> Long) {
    private var status = FreeTimerStatus.READY
    private var durationSeconds = 0
    private var remainingMillis = 0L
    private var deadlineMillis = 0L

    fun start(hours: Int, minutes: Int, seconds: Int): FreeTimerView {
        require(hours in 0..99 && minutes in 0..59 && seconds in 0..59) {
            "Set hours (00–99), minutes (00–59) and seconds (00–59)."
        }
        val duration = hours * 3600 + minutes * 60 + seconds
        require(duration > 0) { "Choose at least one second." }
        durationSeconds = duration
        remainingMillis = duration.toLong() * 1_000L
        deadlineMillis = nowMillis() + remainingMillis
        status = FreeTimerStatus.RUNNING
        return view()
    }

    fun pause(): FreeTimerView {
        if (status == FreeTimerStatus.RUNNING) {
            remainingMillis = (deadlineMillis - nowMillis()).coerceAtLeast(0L)
            status = FreeTimerStatus.PAUSED
        }
        return view()
    }

    fun resume(): FreeTimerView {
        if (status == FreeTimerStatus.PAUSED) {
            deadlineMillis = nowMillis() + remainingMillis
            status = FreeTimerStatus.RUNNING
        }
        return view()
    }

    /** Exit does not trigger a sound or alter any Class. */
    fun exit(): FreeTimerView {
        status = FreeTimerStatus.READY
        remainingMillis = 0L
        durationSeconds = 0
        return view()
    }

    fun update(): FreeTimerTick {
        if (status == FreeTimerStatus.RUNNING && nowMillis() >= deadlineMillis) {
            remainingMillis = 0L
            status = FreeTimerStatus.FINISHED
            return FreeTimerTick(view(), completedNow = true)
        }
        return FreeTimerTick(view())
    }

    fun view(): FreeTimerView {
        val left = if (status == FreeTimerStatus.RUNNING)
            (deadlineMillis - nowMillis()).coerceAtLeast(0L) else remainingMillis
        return FreeTimerView(status, durationSeconds,
            ((left + 999L) / 1_000L).toInt())
    }
}
