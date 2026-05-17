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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val sensoryManager = remember { com.example.smartmail.domain.util.SensoryManager(context) }

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
                color = PremiumAccent,
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PremiumAccent)
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

                    // الفلتر الزجاجي الساحر ✨
                    val categories = listOf("All", "Urgent", "Work", "Tech", "Spam")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = state.selectedCategory == category
                            Surface(
                                onClick = {
                                    viewModel.onEvent(HomeUiEvent.OnCategorySelected(category))
                                    sensoryManager.vibrateLightClick()
                                },
                                shape = RoundedCornerShape(100.dp), // شكل كبسولة دائرية تماماً
                                color = if (isSelected) TextPrimary else CardSurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, if (isSelected) TextPrimary else CardBorder
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) BackgroundDark else TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
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
                                                        // 👇 إحساس الحذف
                                                        sensoryManager.vibrateDelete()
                                                        sensoryManager.playSwipeSound()
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
                containerColor = if (isMagicMode) PremiumAccent else PremiumAccent,
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
                onEvent = { viewModel.onEvent(it) },
                sensoryManager = sensoryManager
            )
        }
    }
}

// =====================================================================
// المكونات الفرعية (Components)
// =====================================================================

// 🏷️ الترويسة الأنيقة
// 🏷️ الترويسة الفخمة جداً (VIP Header)
@Composable
fun HomeHeader(totalMails: Int, urgentCount: Int, userName: String?, userPhotoUrl: String?, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Inbox", color = TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
            Text(text = "$totalMails messages • AI active", color = PremiumAccent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        // 👇 التصميم الاحترافي ذو الطبقات الثلاث (Triple Layer Avatar)
        Box(
            modifier = Modifier
                .size(54.dp) // الحجم الإجمالي للزر
                .clip(CircleShape)
                .clickable { onProfileClick() }
                // 1. الحلقة الخارجية: تدرج لوني يعطي إحساساً بالفخامة
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            PremiumAccent,
                            PremiumAccent.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(2.5.dp), // سمك الحلقة الملونة
            contentAlignment = Alignment.Center
        ) {
            // 2. الفجوة السلبية (Negative Space)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(BackgroundDark) // نفس لون خلفية الشاشة لتبدو كفراغ
                    .padding(3.5.dp), // سمك الفجوة السوداء
                contentAlignment = Alignment.Center
            ) {
                // 3. الصورة الشخصية
                coil.compose.AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(CardSurfaceDark),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}



@Composable
fun AiNewsTickerMode(mails: List<SmartMailEntity>) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start) {
        Text(text = "AI EXECUTIVE SUMMARY", color = PremiumAccent, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
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
    val indicatorColor = when (mail.aiCategory.lowercase()) {
        "work" -> PremiumAccent
        "tech" -> Color(0xFF8B5CF6)
        "spam" -> TextSecondary
        else -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp), // زوايا أكثر نعومة
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {

            // السطر العلوي: المرسل والوقت
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(indicatorColor)) // نقطة لونيّة هادئة
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = mail.senderName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mail.isUrgent) {
                        Surface(color = UrgentRed.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Text("URGENT", color = UrgentRed, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = mail.uiTimeFormatted, color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // عنوان الإيميل (بارز جداً)
            Text(text = mail.subject, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 17.sp, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // خلاصة الذكاء الاصطناعي (أنيقة وغير مزعجة)
            Surface(color = BackgroundDark, shape = RoundedCornerShape(12.dp)) { // خلفية سوداء داخل البطاقة الفحمية
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PremiumAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = mail.aiSummary, color = TextSecondary, fontSize = 13.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, lineHeight = 18.sp)
                }
            }
        }
    }
}


// 📖 تصميم شاشة قراءة الإيميل من الداخل (مع تأثير Aura Theming الخرافي)
@Composable
fun MailDetailSheetContent(
    mail: SmartMailEntity,
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    sensoryManager: com.example.smartmail.domain.util.SensoryManager
) {
    var draftText by remember(mail.id) { mutableStateOf("") }
    val scrollState = androidx.compose.foundation.rememberScrollState()
    var isXRayMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Header (Sender Info)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mail.senderName, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(text = mail.senderEmail, color = TextSecondary, fontSize = 14.sp)
            }
            Text(text = mail.uiTimeFormatted, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = CardBorder)
        Spacer(modifier = Modifier.height(24.dp))

        // 2. Subject & AI Summary
        Text(text = mail.subject, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, lineHeight = 32.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Surface(color = PremiumAccent.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PremiumAccent.copy(alpha = 0.2f))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PremiumAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = mail.aiSummary, color = PremiumAccent, fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 3. X-Ray Toggle & Full Body
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Message Content", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Surface(
                onClick = {
                    isXRayMode = !isXRayMode
                    sensoryManager.vibrateLightClick()
                },
                shape = RoundedCornerShape(20.dp),
                color = if (isXRayMode) PremiumAccent.copy(alpha = 0.15f) else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isXRayMode) PremiumAccent else CardBorder)
            ) {
                Text(
                    text = if (isXRayMode) "X-Ray Active" else "Enable X-Ray",
                    color = if (isXRayMode) PremiumAccent else TextSecondary,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isXRayMode) {
            Text(text = buildXRayText(mail.fullBody), fontSize = 16.sp, lineHeight = 26.sp)
        } else {
            Text(text = mail.fullBody, color = TextPrimary.copy(alpha = 0.9f), fontSize = 16.sp, lineHeight = 26.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 4. Quick Reply Section (Minimalist Version)
        AnimatedVisibility(visible = !state.isReplying) {
            Button(
                onClick = { onEvent(HomeUiEvent.OnQuickReplyClicked) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("Smart Reply ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        AnimatedVisibility(visible = state.isReplying) {
            Column(modifier = Modifier.fillMaxWidth().background(CardSurfaceDark, RoundedCornerShape(20.dp)).border(1.dp, CardBorder, RoundedCornerShape(20.dp)).padding(20.dp)) {

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PremiumAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Context: ${state.currentContextRule}", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f), lineHeight = 18.sp)
                }

                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(onClick = { draftText = "Sorry, ${state.currentContextRule}" }, shape = RoundedCornerShape(100.dp), color = BackgroundDark, border = androidx.compose.foundation.BorderStroke(1.dp, UrgentRed.copy(alpha=0.5f))) {
                            Text("Decline", color = UrgentRed, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        Surface(onClick = { draftText = "I'll review this later." }, shape = RoundedCornerShape(100.dp), color = BackgroundDark, border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)) {
                            Text("Later", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        Surface(onClick = { draftText = "Noted. Proceeding." }, shape = RoundedCornerShape(100.dp), color = BackgroundDark, border = androidx.compose.foundation.BorderStroke(1.dp, PremiumAccent.copy(alpha=0.5f))) {
                            Text("Proceed", color = PremiumAccent, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = draftText, onValueChange = { draftText = it },
                    placeholder = { Text("Custom reply...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PremiumAccent, unfocusedBorderColor = CardBorder),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2, maxLines = 4
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (state.generatedReply.isNotEmpty()) {
                    Surface(color = PremiumAccent.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PremiumAccent.copy(alpha = 0.2f))) {
                        Text(text = state.generatedReply, color = PremiumAccent, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onEvent(HomeUiEvent.OnDismissMailDetail) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumAccent)
                    ) {
                        Text("Send Email", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onEvent(HomeUiEvent.OnCancelReply) }) { Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onEvent(HomeUiEvent.OnGenerateReplyClicked(draftText))
                                sensoryManager.playMagicSound()
                                sensoryManager.vibrateSuccess()
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                            enabled = draftText.isNotEmpty() && !state.isGeneratingReply
                        ) {
                            if (state.isGeneratingReply) CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Generate ✨", color = BackgroundDark, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp).windowInsetsPadding(WindowInsets.navigationBars))
    }
}




@Composable
fun ZenModeView() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "breathing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inbox Zero",
            color = TextPrimary.copy(alpha = alpha), // النص هو الذي يتنفس الآن
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You're all caught up.\nEnjoy your day.",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// 👓 دالة الأشعة السينية (X-Ray) بتوحيد الألوان
@Composable
fun buildXRayText(fullText: String): androidx.compose.ui.text.AnnotatedString {
    val dateRegex = "\\b(\\d{1,2}/\\d{1,2}/\\d{4}|tomorrow|today|Monday|Tuesday|Wednesday|Thursday|Friday)\\b".toRegex(RegexOption.IGNORE_CASE)
    val moneyRegex = "\\$?\\b\\d+(?:,\\d{3})*(?:\\.\\d{2})?\\b".toRegex()
    val emailRegex = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}".toRegex()

    val highlightColor = PremiumAccent // استخدام لون واحد للأشعة
    val dimmedColor = TextSecondary.copy(alpha = 0.3f)

    return androidx.compose.ui.text.buildAnnotatedString {
        withStyle(androidx.compose.ui.text.SpanStyle(color = dimmedColor)) { append(fullText) }

        val style = androidx.compose.ui.text.SpanStyle(color = highlightColor, fontWeight = FontWeight.Black, background = highlightColor.copy(alpha = 0.1f))

        dateRegex.findAll(fullText).forEach { matchResult -> addStyle(style, matchResult.range.first, matchResult.range.last + 1) }
        moneyRegex.findAll(fullText).forEach { matchResult -> addStyle(style, matchResult.range.first, matchResult.range.last + 1) }
        emailRegex.findAll(fullText).forEach { matchResult -> addStyle(style, matchResult.range.first, matchResult.range.last + 1) }
    }
}