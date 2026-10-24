package com.ateliertime.android

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.MelodyPattern
import com.ateliertime.domain.MelodySelections
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Isolated preference namespace; never touches the user's real presets or melody settings. */
@RunWith(AndroidJUnit4::class)
class MelodyPreferencesTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val isolatedName = "atelier_melody_test_" + UUID.randomUUID().toString()

    @After fun cleanup() {
        context.deleteSharedPreferences(isolatedName)
    }

    @Test fun threeIndependentChoicesSurviveNewRepositoryInstance() {
        val initial = MelodyPreferences(context, isolatedName)
        val chosen = MelodySelections(
            poseEnd = MelodyPattern.CALM_DESCENT,
            breakEnd = MelodyPattern.WARM_BELLS,
            sessionEnd = MelodyPattern.BRIGHT_FANFARE,
        )
        initial.save(chosen)
        assertEquals(chosen, MelodyPreferences(context, isolatedName).load())
    }
}
