package com.alteregoai.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteregoai.app.core.AppConstants
import com.alteregoai.app.core.XPSystem
import com.alteregoai.app.data.AlterEgoProfileEntity
import com.alteregoai.app.data.AchievementEntity
import com.alteregoai.app.data.JournalEntryEntity
import com.alteregoai.app.data.MissionEntity

@Composable
fun CinematicBackground(modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background.red < .5f
    val colors = if (dark) listOf(Color(0xFF070812), Color(0xFF21104C), Color(0xFF061729)) else listOf(Color(0xFFF7F8FF), Color(0xFFE8E4FF), Color(0xFFE5FAFA))
    Box(modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(AlterEgoColors.cyan.copy(alpha = if (dark) .15f else .10f), Color.Transparent), radius = 900f)))
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .82f)), border = androidx.compose.foundation.BorderStroke(1.dp, AlterEgoColors.cyan.copy(alpha = .16f)), content = { Column(Modifier.padding(16.dp), content = content) })
}

@Composable
fun PrimaryButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(17.dp)) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    GlassCard(modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.AutoAwesome, null, tint = AlterEgoColors.cyan, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
fun AvatarGlyph(stage: String, modifier: Modifier = Modifier) {
    val symbol = when (stage) { "AWAKENING" -> "↗"; "FOCUSED" -> "◎"; "DISCIPLINED" -> "◇"; "ASCENDANT" -> "✦"; "APEX_SELF" -> "♛"; else -> "◐" }
    Box(modifier.size(76.dp).clip(CircleShape).background(Brush.linearGradient(listOf(AlterEgoColors.cyan, AlterEgoColors.blue, AlterEgoColors.purple))).semantics { contentDescription = stage }) { Text(symbol, fontSize = 32.sp, color = Color(0xFF05060D), fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center)) }
}

@Composable
fun AlterEgoCard(alterEgo: AlterEgoProfileEntity, subtitle: String, modifier: Modifier = Modifier) {
    GlassCard(modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(22.dp), ambientColor = AlterEgoColors.purple.copy(alpha = .5f), spotColor = AlterEgoColors.purple.copy(alpha = .5f))) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(alterEgo.alterEgoName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 4.dp))
            }
            AvatarGlyph(alterEgo.avatarStage)
        }
        Text(alterEgo.identityStatement, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .82f), modifier = Modifier.padding(top = 16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
            Surface(shape = RoundedCornerShape(50), color = AlterEgoColors.cyan.copy(alpha = .14f), border = androidx.compose.foundation.BorderStroke(1.dp, AlterEgoColors.cyan.copy(alpha = .28f))) { Text("⚡ Level ${alterEgo.level}  ${XPSystem.title(alterEgo.level)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) }
            Spacer(Modifier.weight(1f))
            Text("${alterEgo.totalXp} XP", color = AlterEgoColors.cyan, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MissionCard(mission: MissionEntity, onComplete: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .08f)), contentAlignment = Alignment.Center) { Text(if (mission.completed) "✓" else "◎", color = if (mission.completed) AlterEgoColors.success else AlterEgoColors.cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(mission.title, fontWeight = FontWeight.Bold, color = if (mission.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = .55f) else MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${mission.missionCategory().title}  •  ${mission.missionDifficulty().title}  •  +${mission.xpReward} XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 5.dp))
            }
            androidx.compose.material3.IconButton(onClick = onComplete, enabled = !mission.completed) { Icon(if (mission.completed) Icons.Default.CheckCircle else Icons.Default.CheckCircle, if (mission.completed) "Mission completed" else "Complete mission", tint = if (mission.completed) AlterEgoColors.success else MaterialTheme.colorScheme.onSurface.copy(alpha = .54f)) }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier = Modifier, title: String, value: String, color: Color = AlterEgoColors.cyan) {
    GlassCard(modifier) { Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color); Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 5.dp)) }
}

@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "xp progress")
    Box(modifier.size(116.dp).semantics { contentDescription = "Next level ${("${(animated * 100).toInt()} percent")}" }) {
        Canvas(Modifier.fillMaxSize()) { drawArc(Color.White.copy(alpha = .12f), -90f, 360f, false, style = Stroke(12.dp.toPx())); drawArc(Brush.sweepGradient(listOf(AlterEgoColors.cyan, AlterEgoColors.blue, AlterEgoColors.purple)), -90f, animated * 360f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) { Text("${(animated * 100).toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("Next level", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f)) }
    }
}

@Composable
fun JournalCard(entry: JournalEntryEntity) {
    GlassCard(Modifier.fillMaxWidth()) { Row { Text(entry.journalMood().title, color = AlterEgoColors.cyan, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(entry.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f)) }; Text(entry.aiSummary.ifBlank { entry.content }, modifier = Modifier.padding(top = 9.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84f)) }
}

@Composable
fun StageRow(stage: String, current: String) {
    val stages = listOf("DRIFTER", "AWAKENING", "FOCUSED", "DISCIPLINED", "ASCENDANT", "APEX_SELF")
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) { AvatarGlyph(stage, Modifier.size(38.dp)); Text(stage.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(start = 10.dp)); Spacer(Modifier.weight(1f)); Icon(if (stages.indexOf(stage) <= stages.indexOf(current)) Icons.Default.CheckCircle else Icons.Default.Lock, null, tint = if (stages.indexOf(stage) <= stages.indexOf(current)) AlterEgoColors.success else MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)) }
}

@Composable
fun AchievementRow(achievement: AchievementEntity) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) { Icon(if (achievement.unlocked) Icons.Default.CheckCircle else Icons.Default.Lock, null, tint = if (achievement.unlocked) AlterEgoColors.success else MaterialTheme.colorScheme.onSurface.copy(alpha = .42f)); Column(Modifier.padding(start = 12.dp)) { Text(achievement.title, fontWeight = FontWeight.Bold); Text(achievement.description, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f), modifier = Modifier.padding(top = 3.dp)) } }
}

@Composable
fun ErrorBanner(message: String?) { if (!message.isNullOrBlank()) Surface(color = MaterialTheme.colorScheme.error.copy(alpha = .16f), modifier = Modifier.fillMaxWidth()) { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(14.dp)) } }
