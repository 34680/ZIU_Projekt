package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.Subtask
import com.example.data.TaskEntity
import com.example.ui.screens.AnalyticsCard
import com.example.ui.screens.TaskItemCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        val mockTasks = listOf(
            TaskEntity(
                id = 1,
                title = "Nauka do obrony projektu",
                description = "Przejrzeć 10 heurystyk Nielsena i standard WCAG 2.1 AA",
                dueDateLong = System.currentTimeMillis() + 86400000,
                category = "Nauka",
                priority = "WYSOKI",
                isCompleted = false,
                subtasks = listOf(
                    Subtask("s1", "Zrozumieć heurystykę nr 1", true),
                    Subtask("s2", "Zaprojektować lo-fi/hi-fi wireframes", false)
                ),
                synced = false
            ),
            TaskEntity(
                id = 2,
                title = "Zakupy na kolację (pizza)",
                description = "Mąka Tipo 00 i pomidory San Marzano",
                dueDateLong = System.currentTimeMillis() + 43200000,
                category = "Zakupy",
                priority = "NISKI",
                isCompleted = true,
                synced = true
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    AnalyticsCard(tasks = mockTasks)
                    Spacer(modifier = Modifier.height(16.dp))
                    mockTasks.forEach { task ->
                        TaskItemCard(
                            task = task,
                            onToggleCompletion = {},
                            onNavigateToDetails = {}
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
