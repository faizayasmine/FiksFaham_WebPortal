package com.example.ui.news

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MateriEntity
import com.example.data.model.QaPost
import com.example.data.model.QaReply
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.CommunityRulesUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Layar Ruang Diskusi Q&A Per Materi (Revisi Bagian 5).
 * Dipisahkan dari tab Komunitas & Berita dan dibuka dari Burger Menu atau Halaman Materi/Sub-Materi.
 */
@Composable
fun DiscussionScreen(
    materiId: Int,
    materis: List<MateriEntity>,
    posts: List<QaPost>,
    repliesByPost: Map<String, List<QaReply>>,
    currentUserUid: String,
    isMentor: Boolean,
    onLoadPosts: (Int) -> Unit,
    onAddPost: (materiId: Int, title: String, content: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onLoadReplies: (postId: String) -> Unit,
    onAddReply: (postId: String, materiId: Int, content: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onToggleReplyHelpful: (QaReply, postAuthorUid: String, onError: (String) -> Unit) -> Unit,
    onReportContent: (targetType: String, targetId: String, ownerUid: String, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDeletePost: (postId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDeleteReply: (QaReply, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMateri = materis.firstOrNull { it.id == materiId }
    val headerTitle = if (activeMateri != null) {
        "Materi ${activeMateri.urutan}: ${activeMateri.judul}"
    } else {
        "Materi $materiId"
    }

    var questionTitle by remember { mutableStateOf("") }
    var questionBody by remember { mutableStateOf("") }
    var noticeText by remember { mutableStateOf<String?>(null) }
    var noticeIsError by remember { mutableStateOf(false) }

    var expandedPostId by remember { mutableStateOf<String?>(null) }
    var replyDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var reportRequest by remember { mutableStateOf<Triple<String, String, String>?>(null) } // targetType, targetId, ownerUid
    var reportReason by remember { mutableStateOf("") }

    LaunchedEffect(materiId) {
        onLoadPosts(materiId)
    }

    if (reportRequest != null) {
        val (targetType, targetId, ownerUid) = reportRequest!!
        AlertDialog(
            onDismissRequest = { reportRequest = null },
            containerColor = FiksScoreCard,
            title = {
                Text(
                    text = "Laporkan Konten",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = FiksText
                )
            },
            text = {
                OutlinedTextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    placeholder = { Text("Alasan laporan (spam / tidak relevan)...", fontSize = 12.sp) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportContent(
                            targetType,
                            targetId,
                            ownerUid,
                            reportReason.trim().ifBlank { "Konten tidak sesuai" },
                            {
                                noticeIsError = false
                                noticeText = "Laporan berhasil dikirim untuk ditinjau mentor."
                            },
                            { err ->
                                noticeIsError = true
                                noticeText = err
                            }
                        )
                        reportReason = ""
                        reportRequest = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FiksScoreRed)
                ) {
                    Text("Kirim", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportRequest = null }) {
                    Text(stringResource(R.string.common_close), fontFamily = PoppinsFontFamily, color = FiksTextMuted)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("discussion_screen")
    ) {
        // Top Bar
        Surface(
            color = FiksScoreCard,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("discussion_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_close),
                        tint = FiksPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.discussion_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FiksText
                    )
                    Text(
                        text = headerTitle,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Composer Form
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.discussion_ask_title),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FiksText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = questionTitle,
                            onValueChange = { questionTitle = it },
                            placeholder = { Text(stringResource(R.string.discussion_question_title_hint), fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FiksPrimary,
                                unfocusedBorderColor = FiksCardBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qa_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = questionBody,
                            onValueChange = { questionBody = it },
                            placeholder = { Text(stringResource(R.string.discussion_question_body_hint), fontSize = 12.sp) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FiksPrimary,
                                unfocusedBorderColor = FiksCardBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qa_body_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    onAddPost(
                                        materiId,
                                        questionTitle,
                                        questionBody,
                                        {
                                            questionTitle = ""
                                            questionBody = ""
                                            noticeIsError = false
                                            noticeText = "Pertanyaan berhasil dikirim!"
                                        },
                                        { err ->
                                            noticeIsError = true
                                            noticeText = err
                                        }
                                    )
                                },
                                enabled = questionTitle.isNotBlank() && questionBody.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("qa_submit_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.discussion_send_question),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (!noticeText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = noticeText!!,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = if (noticeIsError) FiksScoreRed else FiksScoreGreen
                            )
                        }
                    }
                }
            }

            if (posts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Forum, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.discussion_empty),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksTextMuted
                            )
                        }
                    }
                }
            } else {
                items(posts, key = { it.id.ifBlank { "${it.uid}_${it.createdAt}" } }) { post ->
                    val isExpanded = expandedPostId == post.id
                    val replies = repliesByPost[post.id].orEmpty()
                    val canMark = CommunityRulesUtils.canMarkReplyHelpful(post.uid, currentUserUid, isMentor)
                    val dateText = remember(post.createdAt) {
                        SimpleDateFormat("dd MMM HH:mm", Locale("id", "ID")).format(Date(post.createdAt))
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, if (post.hasHelpfulReply) FiksScoreGreen.copy(alpha = 0.5f) else FiksCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(FiksPrimary.copy(alpha = 0.14f))
                                    ) {
                                        Text(
                                            text = post.authorName.ifBlank { "P" }.take(1).uppercase(),
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = FiksPrimary
                                        )
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = post.authorName.ifBlank { "Pembelajar" },
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = FiksText
                                            )
                                            if (post.authorRole == "mentor") {
                                                Surface(shape = RoundedCornerShape(6.dp), color = FiksPrimary.copy(alpha = 0.12f)) {
                                                    Text(
                                                        text = "Mentor",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        color = FiksPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = dateText,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 10.sp,
                                            color = FiksTextMuted
                                        )
                                    }
                                }
                                Row {
                                    IconButton(
                                        onClick = { reportRequest = Triple("qa_post", post.id, post.uid) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Flag, contentDescription = "Laporkan", tint = FiksTextMuted, modifier = Modifier.size(16.dp))
                                    }
                                    if (isMentor || post.uid == currentUserUid) {
                                        IconButton(
                                            onClick = {
                                                onDeletePost(
                                                    post.id,
                                                    { noticeIsError = false; noticeText = "Pertanyaan dihapus." },
                                                    { err -> noticeIsError = true; noticeText = err }
                                                )
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = FiksScoreRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = post.title,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = FiksText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = post.content,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksText,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isExpanded) {
                                            expandedPostId = null
                                        } else {
                                            expandedPostId = post.id
                                            onLoadReplies(post.id)
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = stringResource(R.string.discussion_replies_count, maxOf(post.replyCount, replies.size)),
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = FiksPrimary
                                    )
                                    if (post.hasHelpfulReply) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = FiksScoreGreen.copy(alpha = 0.14f)) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FiksScoreGreen, modifier = Modifier.size(11.dp))
                                                Text(
                                                    text = stringResource(R.string.discussion_helpful_badge),
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = FiksScoreGreen
                                                )
                                            }
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = FiksPrimary
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    HorizontalDivider(color = FiksCardBorder)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    replies.forEach { reply ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (reply.membantu) FiksScoreGreen.copy(alpha = 0.08f) else FiksBg,
                                            border = BorderStroke(1.dp, if (reply.membantu) FiksScoreGreen.copy(alpha = 0.4f) else FiksCardBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(
                                                            text = reply.authorName.ifBlank { "Pembelajar" },
                                                            fontFamily = PoppinsFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = FiksText
                                                        )
                                                        if (reply.authorRole == "mentor") {
                                                            Surface(shape = RoundedCornerShape(6.dp), color = FiksPrimary.copy(alpha = 0.14f)) {
                                                                Text(
                                                                    text = "Mentor",
                                                                    fontFamily = PoppinsFontFamily,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 9.sp,
                                                                    color = FiksPrimary,
                                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (canMark) {
                                                            IconButton(
                                                                onClick = {
                                                                    onToggleReplyHelpful(reply, post.uid) { err ->
                                                                        noticeIsError = true
                                                                        noticeText = err
                                                                    }
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = if (reply.membantu) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                                                    contentDescription = stringResource(R.string.discussion_mark_helpful),
                                                                    tint = if (reply.membantu) FiksScoreGreen else FiksTextMuted,
                                                                    modifier = Modifier.size(15.dp)
                                                                )
                                                            }
                                                        } else if (reply.membantu) {
                                                            Icon(
                                                                imageVector = Icons.Filled.ThumbUp,
                                                                contentDescription = null,
                                                                tint = FiksScoreGreen,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { reportRequest = Triple("qa_reply", reply.id, reply.uid) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Flag, contentDescription = "Laporkan", tint = FiksTextMuted, modifier = Modifier.size(14.dp))
                                                        }
                                                        if (isMentor || reply.uid == currentUserUid) {
                                                            IconButton(
                                                                onClick = {
                                                                    onDeleteReply(
                                                                        reply,
                                                                        { noticeIsError = false; noticeText = "Balasan dihapus." },
                                                                        { err -> noticeIsError = true; noticeText = err }
                                                                    )
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = FiksScoreRed, modifier = Modifier.size(14.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = reply.content,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontSize = 12.sp,
                                                    color = FiksText,
                                                    lineHeight = 17.sp
                                                )
                                                if (reply.membantu) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = stringResource(R.string.discussion_helpful_badge),
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = FiksScoreGreen
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Reply composer
                                    val draft = replyDrafts[post.id].orEmpty()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = draft,
                                            onValueChange = { replyDrafts = replyDrafts + (post.id to it) },
                                            placeholder = { Text(stringResource(R.string.discussion_reply_hint), fontSize = 11.sp) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = FiksPrimary,
                                                unfocusedBorderColor = FiksCardBorder
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                onAddReply(
                                                    post.id,
                                                    materiId,
                                                    draft,
                                                    { replyDrafts = replyDrafts + (post.id to "") },
                                                    { err -> noticeIsError = true; noticeText = err }
                                                )
                                            },
                                            enabled = draft.isNotBlank()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Kirim Balasan",
                                                tint = if (draft.isNotBlank()) FiksPrimary else FiksTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
