package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserRole
import com.example.data.repository.EduRepository
import org.junit.Assert.assertEquals
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
    assertEquals("EduGrade", appName)
  }

  @Test
  fun `verify three default user roles exist`() {
    val roles = EduRepository.DEFAULT_USERS.map { it.role }
    assertTrue(roles.contains(UserRole.STUDENT))
    assertTrue(roles.contains(UserRole.TEACHER))
    assertTrue(roles.contains(UserRole.PRINCIPAL))
  }

  @Test
  fun `verify class 10th mock exams and interactive lessons`() {
    val class10Exams = EduRepository.MOCK_EXAMS.filter { it.grade == 10 }
    assertTrue(class10Exams.isNotEmpty())

    val class10Lessons = EduRepository.INTERACTIVE_LESSONS.filter { it.grade == 10 }
    assertTrue(class10Lessons.isNotEmpty())
  }
}
