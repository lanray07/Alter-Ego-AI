package com.alteregoai.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.systemBarsPadding
import com.alteregoai.app.core.AppConstants
import com.alteregoai.app.data.GoalCategory
import com.alteregoai.app.data.MotivationStyle

@Composable
fun OnboardingScreen(state: AppUiState, viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var currentIdentity by remember { mutableStateOf("") }
    var futureIdentity by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf(setOf(GoalCategory.DISCIPLINE, GoalCategory.FOCUS, GoalCategory.FITNESS)) }
    var style by remember { mutableStateOf(MotivationStyle.CINEMATIC) }
    var availability by remember { mutableIntStateOf(30) }
    var reminders by remember { mutableStateOf(true) }
    val canSubmit = name.isNotBlank() && currentIdentity.isNotBlank() && futureIdentity.isNotBlank() && goals.size == 3

    Box(Modifier.fillMaxSize()) {
        CinematicBackground()
        Column(Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(AppConstants.viralHook.uppercase(), color = AlterEgoColors.cyan, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Text(AppConstants.appName, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Text(AppConstants.tagline, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .78f))

        OutlinedTextField(name, { name = it }, label = { Text("Name") }, placeholder = { Text("What should your future self call you?") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(currentIdentity, { currentIdentity = it }, label = { Text("Current identity") }, placeholder = { Text("Describe the version of you that exists today.") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedTextField(futureIdentity, { futureIdentity = it }, label = { Text("Future identity") }, placeholder = { Text("Describe who you are becoming.") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        GlassCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Top 3 goals", fontWeight = FontWeight.Bold); Text("${goals.size}/3", color = AlterEgoColors.cyan, fontWeight = FontWeight.Bold) }
            GoalCategory.entries.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { goal ->
                        FilterChip(selected = goals.contains(goal), onClick = { goals = if (goals.contains(goal)) goals - goal else if (goals.size < 3) goals + goal else goals }, label = { Text(goal.title, maxLines = 1) }, modifier = Modifier.weight(1f))
                    }
                    if (pair.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Text("Motivation style", fontWeight = FontWeight.Bold)
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MotivationStyle.entries.forEach { option -> FilterChip(selected = style == option, onClick = { style = option }, label = { Text(option.title) }) } }
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Daily availability"); Text("$availability min", color = AlterEgoColors.cyan) }
            Slider(value = availability.toFloat(), onValueChange = { availability = (it / 5).toInt() * 5 }, valueRange = 10f..120f, steps = 21)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Daily mission reminders"); Switch(checked = reminders, onCheckedChange = { reminders = it }) }
        }

        GlassCard(Modifier.fillMaxWidth()) { Text("AI coaching disclaimer", fontWeight = FontWeight.Bold); Text(AppConstants.coachingDisclaimer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 8.dp)) }
        ErrorBanner(state.errorMessage)
            PrimaryButton(text = if (state.isWorking) "Generating future self…" else "Create my Alter Ego  →", onClick = { viewModel.createOnboarding(name, currentIdentity, futureIdentity, goals.toList().sortedBy { it.title }, style, availability, reminders) }, enabled = canSubmit && !state.isWorking)
        }
    }
}
