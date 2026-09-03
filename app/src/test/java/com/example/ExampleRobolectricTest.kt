package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.SeedPhraseGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Calyx", appName)
  }

  @Test
  fun `verify 24 word seed phrase generation`() {
    val seed = SeedPhraseGenerator.generate24Words()
    val words = seed.split(" ")
    assertEquals(24, words.size)
  }

  @Test
  fun `verify pin hashing is deterministic and irreversible`() {
    val hash1 = SeedPhraseGenerator.hashPin("1234")
    val hash2 = SeedPhraseGenerator.hashPin("1234")
    val hashDecoy = SeedPhraseGenerator.hashPin("0000")

    assertEquals(hash1, hash2)
    assertNotEquals(hash1, hashDecoy)
    assertTrue(hash1.length == 64) // SHA-256 hex length
  }

  @Test
  fun `verify onboarding string resources exist and are populated`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val slide1Title = context.getString(R.string.onboarding_slide1_title)
    val slide2Title = context.getString(R.string.onboarding_slide2_title)
    val slide3Title = context.getString(R.string.onboarding_slide3_title)
    val startBtn = context.getString(R.string.onboarding_start)

    assertTrue(slide1Title.isNotEmpty())
    assertTrue(slide2Title.isNotEmpty())
    assertTrue(slide3Title.isNotEmpty())
    assertTrue(startBtn.isNotEmpty())
  }
}
