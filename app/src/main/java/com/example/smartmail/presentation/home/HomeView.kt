package com.example.smartmail.presentation.home

import android.R.attr.scaleX
import android.R.attr.scaleY
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartmail.data.local.SmartMailEntity
import com.example.smartmail.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isMagicMode by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.onEvent(HomeUiEvent.RefreshMails) },
        state = pullRefreshState,
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = state.isLoading,
                state = pullRefreshState,
                color = AccentNeonGreen,
                containerColor = CardSurfaceDark
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            if (state.isLoading && state.allMails.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentNeonGreen)
            } else if (state.error != null && state.allMails.isEmpty()) {
                Text(text = "Error: ${state.error}", color = UrgentRed, modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {

                    // 1. الترويسة الفخمة (Header)
                    HomeHeader(
                        totalMails = state.allMails.size,
                        urgentCount = state.urgentMails.size,
                        userName = state.userName,
                        userPhotoUrl = state.userPhotoUrl,
                        onProfileClick = onProfileClick
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val categories = listOf("All", "Urgent", "Work", "Tech", "Spam")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = state.selectedCategory == category
                            Surface(
                                onClick = { viewModel.onEvent(HomeUiEvent.OnCategorySelected(category)) },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) AccentCyberPink else CardSurfaceDark.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, if (isSelected) AccentCyberPink else TextSecondary.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) BackgroundDark else TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // 3. الأنيميشن بين قائمة الإيميلات العادية ووضع الأخبار السريع
                    Crossfade(targetState = isMagicMode, label = "magic_mode") { magic ->
                        if (magic) {
                            AiNewsTickerMode(mails = state.allMails)
                        } else {
                            // الفلترة الذكية بناءً على الكبسولة المختارة
                            // 👇 استخدام remember يجعل الانتقالล مرئياً وناعماً
                            val filteredMails = remember(state.selectedCategory, state.allMails) {
                                when (state.selectedCategory) {
                                    "All" -> state.allMails
                                    "Urgent" -> state.allMails.filter { it.isUrgent }
                                    else -> state.allMails.filter { it.aiCategory.lowercase() == state.selectedCategory.lowercase() }
                                }
                            }

                            // 👇 الإضافة السحرية: إذا كانت القائمة فارغة، نعرض شاشة المكافأة
                            if (filteredMails.isEmpty()) {
                                ZenModeView()
                            }
                            else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    // 👇 السر هنا: نحن لا نفلتر القائمة! نعطيه كل الإيميلات دائماً
                                    items(state.allMails, key = { it.id }) { mail ->

                                        // نحدد هنا هل هذا الإيميل مسموح له بالظهور أم لا
                                        val isVisible = state.selectedCategory == "All" ||
                                                (state.selectedCategory == "Urgent" && mail.isUrgent) ||
                                                (state.selectedCategory.lowercase() == mail.aiCategory.lowercase())

                                        // 👇 السحر الحقيقي: AnimatedVisibility سيعطينا الانزلاق الناعم إجبارياً
                                        AnimatedVisibility(
                                            visible = isVisible,
                                            enter = androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.fadeIn(),
                                            exit = androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.fadeOut()
                                        ) {
                                            val dismissState = rememberSwipeToDismissBoxState(
                                                confirmValueChange = { dismissValue ->
                                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                                        viewModel.onEvent(HomeUiEvent.OnDeleteSwipe(mail.id))
                                                        true
                                                    } else false
                                                }
                                            )

                                            SwipeToDismissBox(
                                                state = dismissState,
                                                backgroundContent = {
                                                    val color by animateColorAsState(targetValue = if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) UrgentRed.copy(alpha = 0.8f) else Color.Transparent, label = "color")
                                                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                                                        if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(32.dp))
                                                    }
                                                },
                                                content = {
                                                    SmartMailCard(mail = mail, onClick = { viewModel.onEvent(HomeUiEvent.OnMailClicked(mail)) })
                                                }
                                            )
                                        }
                                    }
                                }
                            } // نهاية الـ else
                        }
                    }
                }
            }

            // الزر العائم للذكاء الاصطناعي (Magic Mode)
            FloatingActionButton(
                onClick = { isMagicMode = !isMagicMode },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp),
                containerColor = if (isMagicMode) AccentCyberPink else AccentNeonGreen,
                contentColor = BackgroundDark
            ) {
                Text(text = "✨", fontSize = 24.sp, modifier = Modifier.padding(12.dp))
            }
        }
    }

    // 4. الشاشة المنبثقة لقراءة تفاصيل الإيميل
    if (state.selectedMail != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(HomeUiEvent.OnDismissMailDetail) },
            sheetState = sheetState,
            containerColor = BackgroundDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
        ) {
            MailDetailSheetContent(
                mail = state.selectedMail!!,
                state = state, // 👈 مررنا الحالة
                onEvent = { viewModel.onEvent(it) } // 👈 مررنا الأحداث
            )
        }
    }
}

// =====================================================================
// المكونات الفرعية (Components)
// =====================================================================

@Composable
fun HomeHeader(totalMails: Int, urgentCount: Int, userName: String?, userPhotoUrl: String?, onProfileClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            coil.compose.AsyncImage(
                model = userPhotoUrl, contentDescription = "Profile Picture",
                modifier = Modifier.size(50.dp).clip(CircleShape).background(CardSurfaceDark).clickable { onProfileClick() },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hello, ${userName?.split(" ")?.first() ?: "User"}!",
                    color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(text = "AI Analyzed: $totalMails Mails", color = TextSecondary, fontSize = 14.sp)
            }
        }
        AnimatedVisibility(visible = urgentCount > 0) {
            Surface(color = UrgentRed.copy(alpha = 0.2f), shape = MaterialTheme.shapes.large, border = androidx.compose.foundation.BorderStroke(1.dp, UrgentRed), modifier = Modifier.padding(start = 16.dp)) {
                Text(text = "$urgentCount URGENT", color = UrgentRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
fun AiNewsTickerMode(mails: List<SmartMailEntity>) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start) {
        Text(text = "AI EXECUTIVE SUMMARY", color = AccentCyberPink, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Spacer(modifier = Modifier.height(32.dp))
        if (mails.isEmpty()) {
            Text("No news is good news!", color = TextSecondary)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                items(mails) { mail ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "⚡", fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                        Column {
                            Text(text = mail.senderName, color = TextSecondary, fontSize = 12.sp)
                            Text(text = mail.aiSummary, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartMailCard(mail: SmartMailEntity, onClick: () -> Unit) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(mail.uiCategoryColor)) } catch (e: Exception) { AccentCyberPink }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(categoryColor))
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = mail.senderName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mail.isUrgent) {
                            Surface(color = UrgentRed.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(end = 8.dp)) {
                                Text("URGENT", color = UrgentRed, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text(text = mail.uiTimeFormatted, color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = mail.subject, color = TextPrimary.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = categoryColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Text(text = "✨", fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(text = mail.aiSummary, color = categoryColor, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// 📖 تصميم شاشة قراءة الإيميل من الداخل (مع تأثير Aura Theming الخرافي)
@Composable
fun MailDetailSheetContent(
    mail: SmartMailEntity,
    state: HomeUiState, // 👈 أضفنا هذا لنقرأ حالة الرد
    onEvent: (HomeUiEvent) -> Unit // 👈 أضفنا هذا لنرسل الأوامر
) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(mail.uiCategoryColor)) } catch (e: Exception) { AccentCyberPink }

    // متغير محلي للنص القصير الذي سيكتبه المستخدم
    var draftText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        // ... (احتفظ بالسطور الأولى لمعلومات الإيميل والعنوان كما هي) ...
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = mail.senderName, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = mail.senderEmail, color = TextSecondary, fontSize = 14.sp)
            }
            Text(text = mail.uiTimeFormatted, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = CardSurfaceDark)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = mail.subject, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, lineHeight = 30.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Surface(color = categoryColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
            Text(text = "✨ AI Summary: ${mail.aiSummary}", color = categoryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(12.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = mail.fullBody, color = TextPrimary.copy(alpha = 0.8f), fontSize = 16.sp, lineHeight = 26.sp)
        Spacer(modifier = Modifier.height(40.dp))

        // 👇 السحر البصري لـ Smart Reply
        AnimatedVisibility(visible = !state.isReplying) {
            // الزر العادي يظهر إذا لم نكن نرد
            Button(
                onClick = { onEvent(HomeUiEvent.OnQuickReplyClicked) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
            ) {
                Text("✨ AI Quick Reply", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
            }
        }

        AnimatedVisibility(visible = state.isReplying) {
            // صندوق الرد الذكي يظهر عند الضغط
            Column(
                modifier = Modifier.fillMaxWidth().background(CardSurfaceDark, RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                // شريط النبرة (Slider)
                Text("Tone of voice:", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = state.replyTone,
                    onValueChange = { onEvent(HomeUiEvent.OnToneChanged(it)) },
                    colors = SliderDefaults.colors(thumbColor = categoryColor, activeTrackColor = categoryColor, inactiveTrackColor = BackgroundDark)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Formal", color = TextSecondary, fontSize = 10.sp)
                    Text("Friendly", color = TextSecondary, fontSize = 10.sp)
                    Text("Strict", color = TextSecondary, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // حقل النص
                OutlinedTextField(
                    value = draftText,
                    onValueChange = { draftText = it },
                    placeholder = { Text("Just say 'Yes' or 'No'...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = categoryColor, unfocusedBorderColor = TextSecondary),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // عرض الرد المولد إذا كان موجوداً
                if (state.generatedReply.isNotEmpty()) {
                    Surface(color = AccentNeonGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(text = state.generatedReply, color = AccentNeonGreen, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // أزرار الإجراء
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onEvent(HomeUiEvent.OnCancelReply) }) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Button(
                        onClick = { onEvent(HomeUiEvent.OnGenerateReplyClicked(draftText)) },
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                        enabled = draftText.isNotEmpty() && !state.isGeneratingReply
                    ) {
                        if (state.isGeneratingReply) {
                            CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Generate 🪄", color = BackgroundDark)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
fun ZenModeView() {
    // 1. أنيميشن التنفس (يكبر ويصغر ببطء ونعومة)
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // الأيقونة المتألقة
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(AccentNeonGreen.copy(alpha = 0.1f))
                .border(1.dp, AccentNeonGreen.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                contentDescription = "All Clear",
                modifier = Modifier
                    .size(80.dp)
                    // 👇 التعديل هنا: استخدمنا graphicsLayer مباشرة
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                tint = AccentNeonGreen
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // الرسالة التشجيعية
        Text(
            text = "Inbox Zero!",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You've crushed all your urgent mails.\nTake a deep breath and enjoy your day.",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}