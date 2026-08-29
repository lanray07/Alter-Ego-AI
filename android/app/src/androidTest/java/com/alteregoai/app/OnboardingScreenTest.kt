package com.alteregoai.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun freshInstallShowsProductOnboarding() {
        composeRule.onNodeWithText("Alter Ego AI").assertIsDisplayed()
        composeRule.onNodeWithText("Create my Alter Ego  →").assertIsDisplayed()
    }
}
