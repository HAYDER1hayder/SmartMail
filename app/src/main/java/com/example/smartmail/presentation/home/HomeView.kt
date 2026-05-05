package com.example.smartmail.presentation.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartmail.data.local.SmartMailEntity
import com.example.smartmail.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
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
            androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = state.isLoading,
                state = pullRefreshState,
                color = AccentNeonGreen,
                containerColor = CardSurfaceDark
            )
        }
    ) {
        // الشاشة من الداخل
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 16.dp).padding(top = 16.dp)) {

            if (state.isLoading && state.allMails.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentNeonGreen)
            } else if (state.error != null && state.allMails.isEmpty()) {
                Text(text = "Error: ${state.error}", color = UrgentRed, modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // الترويسة العلوية الثابتة
                    // Header (الترويسة الفخمة)
                    HomeHeader(
                        totalMails = state.allMails.size,
                        urgentCount = state.urgentMails.size,
                        userName = state.userName, // 👈 أضف هذا
                        userPhotoUrl = state.userPhotoUrl // 👈 وأضف هذا
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 🪄 الأنيميشن السلس للتبديل بين القائمة العادية والأخبار العاجلة
                    Crossfade(targetState = isMagicMode, label = "magic_mode") { magic ->
                        if (magic) {
                            // 📰 وضع الأخبار العاجلة (AI Summary)
                            AiNewsTickerMode(mails = state.allMails)
                        } else {
                            // 📇 وضع القائمة العادية (مع السحب للحذف)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 80.dp) // مساحة للزر العائم
                            ) {
                                items(state.allMails, key = { it.id }) { mail ->
                                    // صندوق السحب للحذف
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
                                            val color by animateColorAsState(
                                                targetValue = if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled)
                                                    UrgentRed.copy(alpha = 0.8f) else Color.Transparent,
                                                label = "color"
                                            )
                                            Box(
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(32.dp))
                                                }
                                            }
                                        },
                                        content = {
                                            SmartMailCard(
                                                mail = mail,
                                                onClick = { viewModel.onEvent(HomeUiEvent.OnMailClicked(mail)) }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ✨ الزر العائم السحري
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

    // 📖 الشاشة المنبثقة لقراءة الإيميل (Bottom Sheet)
    if (state.selectedMail != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(HomeUiEvent.OnDismissMailDetail) },
            sheetState = sheetState,
            containerColor = BackgroundDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
        ) {
            MailDetailSheetContent(mail = state.selectedMail!!)
        }
    }
}

// -------------------------------------------------------------
// المكونات الفرعية (Components) المصممة باحترافية
// -------------------------------------------------------------

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

@Composable
fun MailDetailSheetContent(mail: SmartMailEntity) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(mail.uiCategoryColor)) } catch (e: Exception) { AccentCyberPink }
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
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
        Button(
            onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
        ) {
            Text("Quick Reply", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun HomeHeader(totalMails: Int, urgentCount: Int, userName: String?, userPhotoUrl: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically // جعلناها في المنتصف
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 🖼️ صورة المستخدم الدائرية من جوجل
            coil.compose.AsyncImage(
                model = userPhotoUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(CardSurfaceDark),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 👋 الترحيب باسم المستخدم
            Column {
                Text(
                    text = "Hello, ${userName?.split(" ")?.first() ?: "User"}!", // نأخذ الاسم الأول فقط
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "AI Analyzed: $totalMails Mails",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        AnimatedVisibility(visible = urgentCount > 0) {
            Surface(color = UrgentRed.copy(alpha = 0.2f), shape = MaterialTheme.shapes.large, border = androidx.compose.foundation.BorderStroke(1.dp, UrgentRed)) {
                Text(text = "$urgentCount URGENT", color = UrgentRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}