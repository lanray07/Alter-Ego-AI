package com.alteregoai.app.ui

import android.app.Activity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alteregoai.app.core.AppConstants
import com.alteregoai.app.core.BillingState
import com.alteregoai.app.core.DateUtils
import com.alteregoai.app.core.ShareCardRenderer
import com.alteregoai.app.core.XPSystem
import com.alteregoai.app.data.AlterEgoProfileEntity
import com.alteregoai.app.data.GoalCategory
import com.alteregoai.app.data.MissionCategory
import com.alteregoai.app.data.MissionEntity
import com.alteregoai.app.data.Mood
import com.alteregoai.app.data.MotivationStyle
import com.alteregoai.app.data.SubscriptionPlan
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(state: AppUiState, viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val profile = state.profile ?: return
    val alterEgo = state.alterEgo ?: return
    val today = state.missions.filter { it.isToday() }.sortedBy { it.completed }
    val completed = state.missions.count { it.completed }
    val missed = state.missions.count { !it.completed && it.dueDate < DateUtils.startOfTodayMillis() }
    val streak = XPSystem.currentStreak(state.missions)

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item { AlterEgoCard(alterEgo, profile.futureIdentity, Modifier.clickable { onNavigate(Routes.PROFILE) }) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgressRing(XPSystem.progress(alterEgo.totalXp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(Modifier.fillMaxWidth()) { Row { Text("🔥  $streak", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AlterEgoColors.warning); Spacer(Modifier.weight(1f)); Text("Streak", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f)) }; Text("Discipline ${XPSystem.disciplineScore(completed, missed)}", color = AlterEgoColors.cyan, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp)) }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { MetricCard(title = "Completed", value = completed.toString(), color = AlterEgoColors.success, modifier = Modifier.weight(1f)); MetricCard(title = "Missed", value = missed.toString(), color = AlterEgoColors.warning, modifier = Modifier.weight(1f)) }
                }
            }
        }
        item { Text("Quick actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Complete mission" to { today.firstOrNull { !it.completed }?.let(viewModel::completeMission); Unit }, "Talk to future self" to { onNavigate(Routes.CHAT) }, "Journal" to { onNavigate(Routes.JOURNAL) }, "Generate plan" to { viewModel.generatePlan() }).chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { (label, action) -> OutlinedButton(onClick = action, modifier = Modifier.weight(1f), enabled = !state.isWorking) { Text(label, maxLines = 2) } } } }
                OutlinedButton(onClick = { onNavigate(Routes.TIMELINE) }, modifier = Modifier.fillMaxWidth()) { Text("View transformation timeline") }
            }
        }
        item { Text("Today's missions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (today.isEmpty()) item { EmptyState("No missions yet", "Generate today's plan to start building momentum.") }
        else items(today.take(3), key = { it.id }) { mission -> MissionCard(mission, { viewModel.completeMission(mission) }) }
        item {
            GlassCard(Modifier.fillMaxWidth()) { Text("✦  Next AI message", fontWeight = FontWeight.Bold); Text(state.messages.lastOrNull()?.content ?: "Your future self is ready when you are. Ask for the next move.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .82f), modifier = Modifier.padding(top = 8.dp)) }
        }
        item {
            GlassCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Text("✦", color = AlterEgoColors.cyan, fontSize = 25.sp); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text("Unlock Pro transformation tools", fontWeight = FontWeight.Bold); Text("Unlimited AI messages, advanced insights, share cards, and premium themes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f)) }; TextButton(onClick = { onNavigate(Routes.PAYWALL) }) { Text("Explore") } } }
        }
    }
}

@Composable
fun MissionsScreen(state: AppUiState, viewModel: MainViewModel) {
    var selected by remember { mutableStateOf<MissionCategory?>(null) }
    val filtered = state.missions.filter { selected == null || it.missionCategory() == selected }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { MetricCard(title = "Completed", value = state.missions.count { it.completed }.toString(), color = AlterEgoColors.success, modifier = Modifier.weight(1f)); MetricCard(title = "Open", value = state.missions.count { !it.completed }.toString(), color = AlterEgoColors.cyan, modifier = Modifier.weight(1f)) } }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected == null, { selected = null }, label = { Text("All") }); MissionCategory.entries.forEach { category -> FilterChip(selected == category, { selected = category }, label = { Text(category.title) }) } } }
        if (filtered.isEmpty()) item { EmptyState("No missions", "Generate a plan from the dashboard to fill this list.") }
        else items(filtered, key = { it.id }) { mission -> MissionCard(mission, { viewModel.completeMission(mission) }) }
    }
}

@Composable
fun ChatScreen(state: AppUiState, viewModel: MainViewModel) {
    var draft by remember { mutableStateOf("") }
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(state.messages.size) { if (state.messages.isNotEmpty()) scrollState.animateScrollToItem(state.messages.lastIndex) }
    Column(Modifier.fillMaxSize()) {
        Text("Motivational coaching only. Not therapy, diagnosis, treatment, medical, legal, or financial advice.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp))
        LazyColumn(state = scrollState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.messages, key = { it.id }) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.chatRole() == com.alteregoai.app.data.ChatRole.USER) Arrangement.End else Arrangement.Start) { Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp), color = if (message.chatRole() == com.alteregoai.app.data.ChatRole.USER) AlterEgoColors.blue.copy(alpha = .78f) else MaterialTheme.colorScheme.surfaceVariant, border = if (message.chatRole() == com.alteregoai.app.data.ChatRole.USER) null else androidx.compose.foundation.BorderStroke(1.dp, AlterEgoColors.cyan.copy(alpha = .14f))) { Text(message.content, modifier = Modifier.padding(14.dp).fillMaxWidth(if (message.chatRole() == com.alteregoai.app.data.ChatRole.USER) .82f else .9f)) } }
            }
            if (state.isWorking) item { Text("Future self is responding…", style = MaterialTheme.typography.labelSmall, color = AlterEgoColors.cyan) }
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(draft, { draft = it }, placeholder = { Text("Ask your future self for the next move") }, modifier = Modifier.weight(1f), maxLines = 4)
            IconButton(onClick = { viewModel.sendMessage(draft); draft = "" }, enabled = draft.isNotBlank() && !state.isWorking) { Text("↑", fontSize = 28.sp, color = AlterEgoColors.cyan) }
        }
    }
}

@Composable
fun JournalScreen(state: AppUiState, viewModel: MainViewModel) {
    var mood by remember { mutableStateOf(Mood.FOCUSED) }
    var content by remember { mutableStateOf("") }
    var wins by remember { mutableStateOf("") }
    var struggles by remember { mutableStateOf("") }
    var lesson by remember { mutableStateOf("") }
    val canSave = content.isNotBlank() || wins.isNotBlank() || struggles.isNotBlank()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text("How are you arriving today?", fontWeight = FontWeight.Bold)
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Mood.entries.forEach { option -> FilterChip(mood == option, { mood = option }, label = { Text(option.title) }) } }
                OutlinedTextField(content, { content = it }, label = { Text("Daily reflection") }, placeholder = { Text("What did today reveal about your identity?") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), minLines = 4)
                OutlinedTextField(wins, { wins = it }, label = { Text("Wins") }, placeholder = { Text("What did you do right?") }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), minLines = 2)
                OutlinedTextField(struggles, { struggles = it }, label = { Text("Struggles") }, placeholder = { Text("Where did the old pattern show up?") }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), minLines = 2)
                OutlinedTextField(lesson, { lesson = it }, label = { Text("Lesson learned") }, placeholder = { Text("What will you carry into tomorrow?") }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), minLines = 2)
                PrimaryButton(modifier = Modifier.padding(top = 14.dp), text = if (state.isWorking) "Summarizing…" else "Save reflection  ✓", onClick = { viewModel.saveJournal(mood, content, wins, struggles, lesson) { content = ""; wins = ""; struggles = ""; lesson = "" } }, enabled = canSave && !state.isWorking)
            }
        }
        item { Text("Past reflections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (state.journals.isEmpty()) item { EmptyState("No reflections yet", "Write a quick check-in and Alter Ego AI will summarize the pattern.") }
        else items(state.journals, key = { it.id }) { JournalCard(it) }
    }
}

@Composable
fun TimelineScreen(state: AppUiState, viewModel: MainViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item { GlassCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Text("✦  Weekly review", fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); TextButton(onClick = viewModel::generateReview, enabled = !state.isWorking) { Text(if (state.isWorking) "Generating…" else "Generate") } }; Text(state.weeklyReview.ifBlank { "Generate an AI progress summary from missions, streaks, and reflections." }, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .82f), modifier = Modifier.padding(top = 8.dp)) } }
        if (state.snapshots.isEmpty()) item { EmptyState("Timeline is waiting", "Complete missions and journal reflections to build your transformation record.") }
        else items(state.snapshots, key = { it.id }) { snapshot ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) { BoxDot(); HorizontalDivider(Modifier.height(100.dp).width(2.dp), color = Color.White.copy(alpha = .16f)) }; GlassCard(Modifier.weight(1f)) { Row { Text(snapshot.title, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(snapshot.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f)) }; Text(snapshot.summary, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .82f), modifier = Modifier.padding(top = 8.dp)); Text("${snapshot.xp} XP  •  ${snapshot.streak} streak", style = MaterialTheme.typography.labelSmall, color = AlterEgoColors.cyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp)) } }
        }
    }
}

@Composable
private fun BoxDot() { Surface(Modifier.size(12.dp), shape = androidx.compose.foundation.shape.CircleShape, color = AlterEgoColors.cyan) {} }

@Composable
fun InsightsScreen(state: AppUiState, onNavigate: (String) -> Unit) {
    val completed = state.missions.filter { it.completed }
    val missed = state.missions.filter { !it.completed && it.dueDate < DateUtils.startOfTodayMillis() }
    val consistency = XPSystem.disciplineScore(completed.size, missed.size)
    val score = (consistency + (XPSystem.currentStreak(state.missions) * 2).coerceAtMost(20)).coerceAtMost(100)
    val categories = MissionCategory.entries.map { category -> val all = state.missions.filter { it.missionCategory() == category }; Triple(category, all.count { it.completed }, all.size) }.filter { it.third > 1 || it.second > 0 }
    val strongest = categories.maxByOrNull { it.second }?.first?.title ?: "None"
    val weakest = categories.minByOrNull { if (it.third == 0) 0f else it.second.toFloat() / it.third }?.first?.title ?: "None"
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { MetricCard(title = "Consistency", value = "$consistency%", color = AlterEgoColors.success, modifier = Modifier.weight(1f)); MetricCard(title = "Transformation score", value = score.toString(), color = AlterEgoColors.cyan, modifier = Modifier.weight(1f)) } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { MetricCard(title = "Strongest", value = strongest, color = AlterEgoColors.success, modifier = Modifier.weight(1f)); MetricCard(title = "Weakest", value = weakest, color = AlterEgoColors.warning, modifier = Modifier.weight(1f)) } }
        item { GlassCard(Modifier.fillMaxWidth()) { Text("XP trend", fontWeight = FontWeight.Bold); val xp = completed.sumOf { it.xpReward }; Text(if (xp == 0) "Complete missions to chart your progress." else "$xp XP earned across ${completed.size} completed missions.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .78f), modifier = Modifier.padding(top = 8.dp)); LinearProgressIndicator({ (xp % 1000) / 1000f }, Modifier.fillMaxWidth().padding(top = 14.dp), color = AlterEgoColors.cyan) } }
        item { GlassCard(Modifier.fillMaxWidth()) { Text("Weekly consistency", fontWeight = FontWeight.Bold); (0..6).forEach { offset -> val day = DateUtils.shiftMillisByDays(System.currentTimeMillis(), -(6 - offset)); val dayKey = DateUtils.dayKey(day); val count = completed.count { DateUtils.dayKey(it.completedAt ?: it.dueDate) == dayKey }; Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) { Text(DateUtils.shortDayName(day), modifier = Modifier.width(38.dp), style = MaterialTheme.typography.labelSmall); LinearProgressIndicator({ (count / 3f).coerceAtMost(1f) }, Modifier.weight(1f), color = if (count > 0) AlterEgoColors.success else Color.White.copy(alpha = .18f)); Text(" $count", style = MaterialTheme.typography.labelSmall) } } } }
        item { GlassCard(Modifier.fillMaxWidth()) { Text("Category completion", fontWeight = FontWeight.Bold); categories.forEach { (category, done, total) -> Text("${category.title}  $done/$total", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp)); LinearProgressIndicator({ done.toFloat() / total.coerceAtLeast(1) }, Modifier.fillMaxWidth().padding(top = 4.dp), color = if (done.toFloat() / total.coerceAtLeast(1) > .6f) AlterEgoColors.success else AlterEgoColors.cyan) } } }
        item { OutlinedButton(onClick = { onNavigate(Routes.SHARE) }, modifier = Modifier.fillMaxWidth()) { Text("Create a share card") } }
    }
}

@Composable
fun ProfileScreen(state: AppUiState) {
    val alterEgo = state.alterEgo
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        if (alterEgo == null) item { EmptyState("Profile unavailable", "Create an Alter Ego profile from onboarding.") }
        else {
            item { AlterEgoCard(alterEgo, state.profile?.futureIdentity ?: "Future self") }
            item { GlassCard(Modifier.fillMaxWidth()) { Text("Identity statement", fontWeight = FontWeight.Bold); Text(alterEgo.identityStatement, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .82f), modifier = Modifier.padding(top = 8.dp)); Text("Traits", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp)); TagRow(alterEgo.traitList()); Text("Goals", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp)); TagRow(alterEgo.goalList()) } }
            item { GlassCard(Modifier.fillMaxWidth()) { Text("Avatar evolution", fontWeight = FontWeight.Bold); listOf("DRIFTER", "AWAKENING", "FOCUSED", "DISCIPLINED", "ASCENDANT", "APEX_SELF").forEach { StageRow(it, alterEgo.avatarStage) } } }
            item { GlassCard(Modifier.fillMaxWidth()) { Text("Unlocked milestones", fontWeight = FontWeight.Bold); if (state.achievements.isEmpty()) Text("Milestones will appear as your identity compounds.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 8.dp)); state.achievements.forEach { AchievementRow(it) } } }
        }
    }
}

@Composable
private fun TagRow(tags: List<String>) { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { tags.forEach { tag -> Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(50), color = Color.White.copy(alpha = .08f)) { Text(tag, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) } } } }

@Composable
fun ShareCardsScreen(state: AppUiState, viewModel: MainViewModel, container: com.alteregoai.app.core.AppContainer) {
    var selected by remember { mutableStateOf("Day 7 of becoming my alter ego") }
    val templates = listOf("Day 7 of becoming my alter ego", "My future self unlocked Discipline Level 10", "I completed 30 missions this week", "Current identity vs future identity", "My comeback plan")
    val text = state.shareText.ifBlank { selected }
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item { SharePreview(text, state.alterEgo) }
        item { GlassCard(Modifier.fillMaxWidth()) { Text("Choose a template", fontWeight = FontWeight.Bold); templates.forEach { template -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { RadioButton(selected == template, { selected = template }); Text(template) } } } }
        item { PrimaryButton(text = if (state.isWorking) "Generating…" else "Generate share text", onClick = { viewModel.generateShareText(selected) }, enabled = !state.isWorking) }
        item { OutlinedButton(onClick = { ShareCardRenderer.shareText(context, text) }, modifier = Modifier.fillMaxWidth()) { Text("Share text with system sheet") } }
        item { OutlinedButton(onClick = { ShareCardRenderer.renderAndShare(context, text, state.alterEgo) }, modifier = Modifier.fillMaxWidth()) { Text("Share image card") } }
        item { Text("Nothing is shared until you choose Share and confirm in the Android system share sheet.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun SharePreview(text: String, alterEgo: AlterEgoProfileEntity?) {
    GlassCard(Modifier.fillMaxWidth()) { Row { Text(AppConstants.appName, color = AlterEgoColors.cyan, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(AppConstants.viralHook, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .64f)) }; Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 28.dp)); alterEgo?.let { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 28.dp)) { AvatarGlyph(it.avatarStage, Modifier.size(54.dp)); Column(Modifier.padding(start = 12.dp)) { Text(it.alterEgoName, fontWeight = FontWeight.Bold); Text("Level ${it.level} ${it.stage().title}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f)) } } } }
}

@Composable
fun PaywallScreen(container: com.alteregoai.app.core.AppContainer) {
    val billing by container.billing.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item { Text("Become without ceilings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black); Text("Premium unlocks deeper coaching and transformation artifacts while keeping purchases inside Google Play Billing.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 8.dp)) }
        item { PlanCard("Free", "Included", "Start the identity loop.", listOf("3 daily missions", "Basic habit tracking", "Limited AI messages", "7-day timeline"), null, billing, container.billing, context) }
        item { PlanCard("Pro", billing.prices[AppConstants.proMonthlyProductId] ?: "Available in Google Play", "Build momentum without limits.", listOf("Unlimited AI future-self messages", "Custom mission generation", "Advanced insights", "Transformation timeline", "Viral share cards", "Premium avatar themes"), AppConstants.proMonthlyProductId, billing, container.billing, context) }
        item { PlanCard("Pro Yearly", billing.prices[AppConstants.proYearlyProductId] ?: "Available in Google Play", "Commit to the long arc.", listOf("All Pro features", "Yearly billing", "Best value configuration slot"), AppConstants.proYearlyProductId, billing, container.billing, context) }
        item { PlanCard("Elite", billing.prices[AppConstants.eliteMonthlyProductId] ?: "Available in Google Play", "Cinematic identity coaching.", listOf("Advanced AI personalities", "Cinematic identity cards", "Deep weekly reviews", "Premium avatar themes"), AppConstants.eliteMonthlyProductId, billing, container.billing, context) }
        billing.error?.let { error -> item { ErrorBanner(error) } }
        item { OutlinedButton(onClick = container.billing::restore, modifier = Modifier.fillMaxWidth()) { Text("Restore purchases") } }
        item { Text("Prices and trial terms are supplied by Google Play. Server-side purchase verification must be enabled before granting production entitlements.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f)) }
    }
}

@Composable
private fun PlanCard(title: String, price: String, subtitle: String, features: List<String>, productId: String?, billing: BillingState, billingRepository: com.alteregoai.app.core.BillingRepository, context: android.content.Context) {
    GlassCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.Top) { Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp)) }; Text(price, color = AlterEgoColors.cyan, fontWeight = FontWeight.Bold) }; features.forEach { Text("✓  $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84f), modifier = Modifier.padding(top = 9.dp)) }; productId?.let { id -> val activity = context as? Activity; Button(onClick = { if (activity != null) billingRepository.purchase(activity, id) }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp), enabled = billing.prices.containsKey(id) && activity != null) { Text(if (billing.ownedProductIds.contains(id)) "Active" else "Choose $title") } } }
}

@Composable
fun SettingsScreen(state: AppUiState, container: com.alteregoai.app.core.AppContainer, currentTheme: String, onThemeChange: (String) -> Unit, onRemindersChange: (Boolean) -> Unit, onPreferencesChange: (MotivationStyle, Boolean) -> Unit, onViewPlans: () -> Unit, onDelete: () -> Unit, onExport: (android.content.Context) -> Unit) {
    val context = LocalContext.current
    var style by remember(state.profile?.motivationStyle) { mutableStateOf(state.profile?.style() ?: MotivationStyle.CINEMATIC) }
    var reminders by remember(state.profile?.notificationPreference) { mutableStateOf(state.profile?.notificationPreference ?: true) }
    var showDelete by remember { mutableStateOf(false) }
    val billing by container.billing.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 28.dp)) {
        item { GlassCard(Modifier.fillMaxWidth()) { Text("Subscription", fontWeight = FontWeight.Bold); Row(Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Current plan"); Spacer(Modifier.weight(1f)); Text(billing.activePlan.title, color = AlterEgoColors.cyan) }; OutlinedButton(onClick = onViewPlans, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("View plans") }; OutlinedButton(onClick = container.billing::restore, modifier = Modifier.fillMaxWidth()) { Text("Restore purchases") } } }
        item { GlassCard(Modifier.fillMaxWidth()) { Text("Coaching and safety", fontWeight = FontWeight.Bold); Text(AppConstants.coachingDisclaimer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp)); Text("Privacy policy and Terms links must be supplied in SETUP_REQUIRED.md before release.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 12.dp)) } }
        if (state.profile != null) item { GlassCard(Modifier.fillMaxWidth()) { Text("Preferences", fontWeight = FontWeight.Bold); Text("Motivation style", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp)); Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MotivationStyle.entries.forEach { option -> FilterChip(style == option, { style = option; onPreferencesChange(option, reminders) }, label = { Text(option.title) }) } }; Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text("Daily reminders"); Spacer(Modifier.weight(1f)); Switch(reminders, { reminders = it; onPreferencesChange(style, reminders); onRemindersChange(it) }) }; Text("Theme", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 14.dp)); listOf("Cinematic", "Neon Cyan", "Apex Dark", "Light").forEach { option -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { RadioButton(currentTheme == option, { onThemeChange(option) }); Text(option) } } } }
        item { GlassCard(Modifier.fillMaxWidth()) { Text("Data", fontWeight = FontWeight.Bold); OutlinedButton(onClick = { onExport(context) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Export data summary") }; OutlinedButton(onClick = { showDelete = true }, modifier = Modifier.fillMaxWidth()) { Text("Delete all data") } } }
        item { GlassCard(Modifier.fillMaxWidth()) { Text("AI backend", fontWeight = FontWeight.Bold); Text(if (com.alteregoai.app.BuildConfig.AI_BACKEND_URL.isBlank()) "Mock AI is enabled by default. Add AI_BACKEND_URL to use the compatible remote backend. Never store provider keys in the app." else "Remote AI backend configured through build-time AI_BACKEND_URL. Provider keys must remain server-side.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp)) } }
    }
    if (showDelete) AlertDialog(onDismissRequest = { showDelete = false }, title = { Text("Delete all local data?") }, text = { Text("This removes your profile, missions, journals, chat, timeline, achievements, and local subscription cache from this device.") }, confirmButton = { TextButton(onClick = { showDelete = false; onDelete() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } })
}
