package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.NotificationEntity
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

@Composable
fun NotificationDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: ((NotificationEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val unreadCount = notifications.count { !it.isRead }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FiksBg,
            border = BorderStroke(1.dp, FiksCardBorder),
            shadowElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .testTag("notification_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FiksAccent.copy(alpha = 0.25f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Pemberitahuan",
                                tint = FiksPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Pemberitahuan",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FiksText
                            )
                            if (unreadCount > 0) {
                                Text(
                                    text = "$unreadCount belum dibaca",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = FiksPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = FiksTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (notifications.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = FiksTextMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada pemberitahuan baru.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 13.sp,
                                color = FiksTextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(notifications) { notif ->
                            val isCapstone = notif.assessmentId == 999 || notif.judul.contains("Capstone", ignoreCase = true)
                            val isUnread = !notif.isRead

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUnread) FiksScoreCard else FiksScoreCard.copy(alpha = 0.65f),
                                border = BorderStroke(
                                    width = if (isUnread) 1.2.dp else 1.dp,
                                    color = if (isUnread) FiksPrimary.copy(alpha = 0.7f) else FiksCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNotificationClick?.invoke(notif) }
                                    .testTag("notification_item_${notif.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Notification Icon
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCapstone) FiksScoreGreen.copy(alpha = 0.15f)
                                                else FiksAccent.copy(alpha = 0.2f)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isCapstone) Icons.Default.RateReview else Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = if (isCapstone) FiksScoreGreen else FiksPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = notif.judul,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = if (isUnread) FiksText else FiksText.copy(alpha = 0.8f),
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            if (isUnread) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(FiksPrimary)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = notif.pesan,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = FiksText,
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = notif.deadlineInfo,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = if (isCapstone) FiksScoreGreen else FiksAccent
                                            )
                                            Text(
                                                text = "• Ketuk untuk buka",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = FiksTextMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Buka",
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Button: Mark all as read
                FiksButton(
                    text = "TANDAI SEMUA SUDAH DIBACA",
                    onClick = {
                        onMarkAllRead()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mark_all_read_button")
                )
            }
        }
    }
}
