package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GameStatistics
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavyElevated
import com.example.ui.theme.IvoryMuted
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.SoftIvory

@Composable
fun StatsDialog(
    stats: GameStatistics,
    currentLevel: Int,
    coins: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = DarkNavyCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Game Statistics",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = SoftIvory
                            ),
                            modifier = Modifier.testTag("stats_dialog_title")
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("stats_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = IvoryMuted
                        )
                    }
                }

                // Stats List
                StatItem(label = "Levels Completed", value = "${stats.totalLevelsCompleted}")
                StatItem(label = "Current Level Reached", value = "Level $currentLevel")
                StatItem(label = "Total Arrows Cleared", value = "${stats.totalArrowsCleared}")
                StatItem(label = "Hints Used", value = "${stats.totalHintsUsed}")
                StatItem(label = "Blocked Taps / Mistakes", value = "${stats.totalMistakes}")
                StatItem(label = "Daily Challenge Streak", value = "${stats.dailyStreak} days", isHighlight = true)
                StatItem(label = "Current Coins Balance", value = "$coins ¢", isAmber = true)
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isAmber: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkNavyElevated)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = IvoryMuted, fontSize = 14.sp)
        Text(
            text = value,
            color = when {
                isHighlight -> AccentTeal
                isAmber -> AccentAmber
                else -> SoftIvory
            },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
