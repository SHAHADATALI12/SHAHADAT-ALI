package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.JarvisGuideRepository
import com.example.service.CommandExecutionResult
import com.example.service.DeviceAutomationController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("Mr Shahadat", appName)
  }

  @Test
  fun `verify guide repository has articles and blueprints`() {
    assertTrue(JarvisGuideRepository.articles.isNotEmpty())
    assertTrue(JarvisGuideRepository.blueprints.isNotEmpty())
    assertNotNull(JarvisGuideRepository.articles.first().titleHindi)
  }

  @Test
  fun `verify age extraction from english and hindi phrasing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val controller = DeviceAutomationController(context)

    assertEquals(25, controller.extractAgeFromText("I am 25 years old"))
    assertEquals(24, controller.extractAgeFromText("I'm 24 yrs old"))
    assertEquals(30, controller.extractAgeFromText("My age is 30"))
    assertEquals(22, controller.extractAgeFromText("Meri age 22 saal hai"))
    assertEquals(19, controller.extractAgeFromText("Main 19 saal ka hoon"))
    assertEquals(28, controller.extractAgeFromText("Set my age to 28"))
  }

  @Test
  fun `verify age processing and acknowledgment in command execution`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val controller = DeviceAutomationController(context)

    val result = controller.parseAndExecuteCommand("I am 25 years old")
    assertTrue(result is CommandExecutionResult.Handled)
    val handled = result as CommandExecutionResult.Handled
    assertEquals(25, handled.updatedAge)
    assertEquals(25, controller.getUserAge())
    assertTrue(handled.responseText.contains("25"))

    // Test age query
    val queryResult = controller.parseAndExecuteCommand("What is my age?")
    assertTrue(queryResult is CommandExecutionResult.Handled)
    val queryHandled = queryResult as CommandExecutionResult.Handled
    assertTrue(queryHandled.responseText.contains("25"))
  }

  @Test
  fun `verify owner and islamic greeting acknowledgment`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val controller = DeviceAutomationController(context)

    val salamResult = controller.parseAndExecuteCommand("Assalamu Alaikum")
    assertTrue(salamResult is CommandExecutionResult.Handled)
    assertTrue((salamResult as CommandExecutionResult.Handled).responseText.contains("Walaikum Assalam"))
    assertTrue((salamResult as CommandExecutionResult.Handled).responseText.contains("Shahadat Ali"))

    val ownerResult = controller.parseAndExecuteCommand("Who is your owner?")
    assertTrue(ownerResult is CommandExecutionResult.Handled)
    assertTrue((ownerResult as CommandExecutionResult.Handled).responseText.contains("Shahadat Ali"))
  }
}
