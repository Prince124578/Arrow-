package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.SoftIvory

@Composable
fun HeaderBar(
    levelNumber: Int,
    levelTitle: String,
    livesRemaining: Int,
    coins: Int,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pause Button
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .testTag("pause_button")
                .size(44.dp)
                .clip(CircleShape)
                .background(DarkNavyCard)
                .border(1.dp, NavyBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Pause",
                tint = SoftIvory,
                modifier = Modifier.size(22.dp)
            )
        }

        // Center Level & Lives
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Level $levelNumber",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SoftIvory
                ),
                modifier = Modifier.testTag("level_title_text")
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 3 Lives Hearts
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..3) {
                    val isActive = i <= livesRemaining
                    val heartColor by animateColorAsState(
                        targetValue = if (isActive) AccentCoral else Color(0x40CCD6F6),
                        animationSpec = tween(300),
                        label = "heart_color_$i"
                    )
                    Icon(
                        imageVector = if (isActive) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Life $i",
                        tint = heartColor,
                        modifier = Modifier
                            .testTag("heart_indicator_$i")
                            .size(20.dp)
                    )
                }
            }
        }

        // Coins Badge
        Row(
            modifier = Modifier
                .testTag("coin_balance_badge")
                .clip(RoundedCornerShape(20.dp))
                .background(DarkNavyCard)
                .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(AccentAmber),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¢",
                    color = DarkNavyCard,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "$coins",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentAmber,
                    fontSize = 14.sp
                )
            )
        }
    }
}
