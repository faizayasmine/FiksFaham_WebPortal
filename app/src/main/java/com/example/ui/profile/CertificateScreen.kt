package com.example.ui.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.data.model.UserEntity
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.CertificateConfig
import com.example.util.PdfCertificateGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CertificateScreen(
    user: UserEntity?,
    materis: List<MateriEntity>,
    allAttempts: List<AssessmentAttemptEntity>,
    capstoneSubmissions: List<CapstoneSubmissionEntity>,
    onOpenEditProfile: () -> Unit,
    onBack: () -> Unit,
    allAssessments: List<AssessmentEntity> = emptyList(),
    onRegisterCertificateNumber: ((CertificateConfig.GraduationCertificateEligibility, (String?) -> Unit) -> Unit)? = null,
    onVerifyCertificateNumber: ((String, (Result<Map<String, Any?>>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isGeneratingPdf by remember { mutableStateOf(false) }
    var generatingMateriId by remember { mutableStateOf<Int?>(null) }
    var registeredCertNumber by remember { mutableStateOf<String?>(null) }
    var verifyInput by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verifyResultMessage by remember { mutableStateOf<String?>(null) }
    var verifyResultValid by remember { mutableStateOf<Boolean?>(null) }

    // Evaluasi sertifikat tunggal kelulusan Dunia Desain (5 Materi + Capstone Akhir)
    val graduationEligibility = remember(materis, allAttempts, capstoneSubmissions) {
        CertificateConfig.evaluateGraduationEligibility(
            materis = materis,
            allAttempts = allAttempts,
            capstoneSubmissions = capstoneSubmissions
        )
    }
    val isGraduationEligible = graduationEligibility.status is CertificateConfig.EligibilityStatus.Eligible
    val capstoneApprovedAt = remember(capstoneSubmissions) {
        capstoneSubmissions.firstOrNull { it.materiId == CertificateConfig.FINAL_CAPSTONE_MATERI_ID }?.submittedAt
            ?: capstoneSubmissions.firstOrNull()?.submittedAt
            ?: System.currentTimeMillis()
    }

    LaunchedEffect(isGraduationEligible, user?.firebaseUid) {
        if (isGraduationEligible) {
            onRegisterCertificateNumber?.invoke(graduationEligibility) { num ->
                if (!num.isNullOrBlank()) {
                    registeredCertNumber = num
                }
            }
            if (registeredCertNumber.isNullOrBlank()) {
                registeredCertNumber = CertificateConfig.generateCertificateNumber(
                    user?.firebaseUid.orEmpty(),
                    capstoneApprovedAt
                )
            }
        }
    }

    val certificateList = remember(materis, allAttempts, capstoneSubmissions) {
        CertificateConfig.buildCertificateList(materis, allAttempts, capstoneSubmissions)
    }

    val isUserNameEmpty = user?.nama.isNullOrBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .statusBarsPadding()
            .testTag("certificate_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("certificate_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = FiksText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.certificate_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksText
                    )
                    Text(
                        text = stringResource(R.string.certificate_subtitle),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Warning if user's profile name is empty (Requirement 8C d)
            if (isUserNameEmpty) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FiksAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, FiksPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("certificate_empty_name_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.certificate_empty_name_title),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FiksPrimary
                            )
                        }
                        Text(
                            text = stringResource(R.string.certificate_empty_name_desc),
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksText,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = onOpenEditProfile,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                            modifier = Modifier.testTag("open_edit_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.certificate_btn_edit_profile),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sertifikat Kelulusan Tunggal Dunia Desain
            Text(
                text = "Sertifikat Kelulusan Dunia Desain",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FiksText
            )
            Spacer(modifier = Modifier.height(10.dp))

            val graduationStatus = graduationEligibility.status
            val graduationItem = CertificateConfig.CertificateItem(
                materiId = 0,
                materiTitle = "Kurikulum Lengkap Dunia Desain (5 Materi + Capstone)",
                status = graduationStatus
            )
            CertificateCardItem(
                item = graduationItem,
                isUserNameEmpty = isUserNameEmpty,
                isGenerating = isGeneratingPdf && generatingMateriId == 0,
                certificateNumber = registeredCertNumber,
                onGenerateAndShare = {
                    if (isUserNameEmpty) {
                        Toast.makeText(context, "Harap isi nama di Edit Profil terlebih dahulu.", Toast.LENGTH_SHORT).show()
                        onOpenEditProfile()
                        return@CertificateCardItem
                    }
                    if (isGraduationEligible) {
                        isGeneratingPdf = true
                        generatingMateriId = 0
                        coroutineScope.launch {
                            try {
                                val certNum = registeredCertNumber ?: CertificateConfig.generateCertificateNumber(
                                    user?.firebaseUid.orEmpty(),
                                    capstoneApprovedAt
                                )
                                val pdfFile = withContext(Dispatchers.IO) {
                                    PdfCertificateGenerator.generateCertificatePdf(
                                        context = context,
                                        userName = user?.nama.orEmpty(),
                                        materiTitle = "Kurikulum Dunia Desain (5 Materi + Capstone Akhir)",
                                        materiId = 0,
                                        posttestScore = graduationEligibility.averagePosttestScore,
                                        capstoneScore = graduationEligibility.capstoneScore ?: 0,
                                        certificateNumber = certNum
                                    )
                                }
                                PdfCertificateGenerator.shareCertificate(
                                    context = context,
                                    file = pdfFile,
                                    materiTitle = "Kelulusan Dunia Desain"
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isGeneratingPdf = false
                                generatingMateriId = null
                            }
                        }
                    }
                },
                onPromptEditProfile = onOpenEditProfile
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Verifikasi Nomor Sertifikat
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("certificate_verify_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verifikasi Nomor Sertifikat",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Masukkan nomor sertifikat (contoh: FIKS-...) untuk memeriksa keasliannya.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = verifyInput,
                        onValueChange = { verifyInput = it },
                        singleLine = true,
                        placeholder = { Text("FIKS-...", fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("certificate_verify_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (verifyInput.isBlank()) return@Button
                            isVerifying = true
                            verifyResultMessage = null
                            onVerifyCertificateNumber?.invoke(verifyInput.trim()) { res ->
                                isVerifying = false
                                res.onSuccess { data ->
                                    val owner = (data["nama"] as? String)?.ifBlank { null }
                                        ?: (data["recipientName"] as? String)
                                        ?: "Peserta Terdaftar"
                                    val certStatus = (data["status"] as? String)?.trim()?.lowercase() ?: "active"
                                    val predikat = (data["predikat"] as? String).orEmpty()
                                    val score = (data["capstoneScore"] as? Number)?.toInt()
                                    if (certStatus == "revoked") {
                                        verifyResultValid = false
                                        verifyResultMessage = "Sertifikat atas nama $owner telah DICABUT oleh pengelola portal dan tidak lagi berlaku."
                                    } else {
                                        verifyResultValid = true
                                        val scorePart = score?.let { " (Capstone: $it/100)" }.orEmpty()
                                        val predikatPart = if (predikat.isNotBlank()) " • Predikat: $predikat" else ""
                                        verifyResultMessage = "Terverifikasi atas nama $owner$scorePart$predikatPart"
                                    }
                                }.onFailure { err ->
                                    verifyResultValid = false
                                    verifyResultMessage = err.message ?: "Nomor sertifikat tidak ditemukan."
                                }
                            } ?: run {
                                isVerifying = false
                                verifyResultValid = false
                                verifyResultMessage = "Verifikasi online belum tersedia dalam mode ini."
                            }
                        },
                        enabled = verifyInput.isNotBlank() && !isVerifying,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("certificate_verify_button")
                    ) {
                        Text(
                            text = if (isVerifying) "Memeriksa..." else "Verifikasi Sertifikat",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    if (!verifyResultMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = verifyResultMessage!!,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (verifyResultValid == true) FiksScoreGreen else FiksScoreRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ringkasan Kelulusan per Materi (Opsional Transkrip)
            Text(
                text = "Rincian Progress 5 Materi (${graduationEligibility.passedPosttestCount}/${graduationEligibility.totalMateriCount} Posttest Lulus)",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = FiksText
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                certificateList.forEach { certItem ->
                    CertificateCardItem(
                        item = certItem,
                        isUserNameEmpty = isUserNameEmpty,
                        isGenerating = isGeneratingPdf && generatingMateriId == certItem.materiId,
                        onGenerateAndShare = {
                            if (isUserNameEmpty) {
                                Toast.makeText(context, "Harap isi nama di Edit Profil terlebih dahulu.", Toast.LENGTH_SHORT).show()
                                onOpenEditProfile()
                                return@CertificateCardItem
                            }

                            val eligibleStatus = certItem.status as? CertificateConfig.EligibilityStatus.Eligible
                            if (eligibleStatus != null) {
                                isGeneratingPdf = true
                                generatingMateriId = certItem.materiId
                                coroutineScope.launch {
                                    try {
                                        val pdfFile = withContext(Dispatchers.IO) {
                                            PdfCertificateGenerator.generateCertificatePdf(
                                                context = context,
                                                userName = user?.nama.orEmpty(),
                                                materiTitle = certItem.materiTitle,
                                                materiId = certItem.materiId,
                                                posttestScore = eligibleStatus.posttestScore,
                                                capstoneScore = eligibleStatus.capstoneScore
                                            )
                                        }
                                        PdfCertificateGenerator.shareCertificate(
                                            context = context,
                                            file = pdfFile,
                                            materiTitle = certItem.materiTitle
                                        )
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingPdf = false
                                        generatingMateriId = null
                                    }
                                }
                            }
                        },
                        onPromptEditProfile = onOpenEditProfile
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CertificateCardItem(
    item: CertificateConfig.CertificateItem,
    isUserNameEmpty: Boolean,
    isGenerating: Boolean,
    certificateNumber: String? = null,
    onGenerateAndShare: () -> Unit,
    onPromptEditProfile: () -> Unit
) {
    val isEligible = item.status is CertificateConfig.EligibilityStatus.Eligible

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, if (isEligible) FiksScoreGreen.copy(alpha = 0.4f) else FiksCardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("certificate_item_${item.materiId}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isEligible) FiksScoreGreen.copy(alpha = 0.14f) else Color(0xFF6B5A4D).copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isEligible) Icons.Default.Verified else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isEligible) FiksScoreGreen else FiksTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isEligible) FiksScoreGreen.copy(alpha = 0.15f) else FiksTextMuted.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isEligible) stringResource(R.string.common_available) else stringResource(R.string.common_locked),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isEligible) FiksScoreGreen else FiksTextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.materiTitle,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = FiksCardBorder.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            when (val status = item.status) {
                is CertificateConfig.EligibilityStatus.Eligible -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Posttest: ${status.posttestScore}/100 • Capstone: ${status.capstoneScore}/100",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksScoreGreen
                            )
                            if (!certificateNumber.isNullOrBlank()) {
                                Text(
                                    text = "No: $certificateNumber",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = FiksPrimary
                                )
                            }
                            Text(
                                text = stringResource(R.string.certificate_status_available),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                        }

                        Button(
                            onClick = onGenerateAndShare,
                            enabled = !isGenerating,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                            modifier = Modifier.testTag("download_cert_button_${item.materiId}")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.common_share),
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                is CertificateConfig.EligibilityStatus.Locked -> {
                    val reasonDesc = when (status.reasonKey) {
                        CertificateConfig.ReasonKey.ALL_POSTTESTS_NOT_PASSED -> "Lulus semua 5 Posttest Materi terlebih dahulu (nilai >= 75)."
                        CertificateConfig.ReasonKey.POSTTEST_NOT_PASSED -> stringResource(R.string.certificate_reason_posttest_not_passed)
                        CertificateConfig.ReasonKey.CAPSTONE_NOT_SUBMITTED -> stringResource(R.string.certificate_reason_capstone_not_submitted)
                        CertificateConfig.ReasonKey.CAPSTONE_UNDER_REVIEW -> stringResource(R.string.certificate_reason_capstone_under_review)
                        CertificateConfig.ReasonKey.CAPSTONE_REVISION_NEEDED -> "Capstone perlu direvisi terlebih dahulu sesuai catatan Mentor."
                        CertificateConfig.ReasonKey.CAPSTONE_REJECTED -> "Capstone berstatus ditolak final. Hubungi Fasilitator atau Super Admin untuk informasi lebih lanjut."
                        CertificateConfig.ReasonKey.CAPSTONE_SCORE_LOW -> stringResource(
                            R.string.certificate_reason_capstone_score_low,
                            status.currentCapstoneScore ?: 0
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = FiksTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = reasonDesc,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = FiksTextMuted,
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
