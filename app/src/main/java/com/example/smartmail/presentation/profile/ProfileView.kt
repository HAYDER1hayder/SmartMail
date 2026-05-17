package com.example.smartmail.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartmail.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onScheduleClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. بطاقة الهوية الأفقية (Horizontal ID Card) - فخمة وهادئة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // الصورة الشخصية بحجم منطقي وأنيق (64dp)
                coil.compose.AsyncImage(
                    model = userData?.profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(1.dp, CardBorder, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                // الاسم والإيميل (أو خطة الدفع)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userData?.username ?: "SmartMail User",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Premium Plan (Active)", // نص بديل لرمز הـ userId المزعج
                        color = PremiumAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. قائمة الإعدادات (Settings List)
            Text(
                text = "APP PREFERENCES",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                ProfileMenuItem(icon = Icons.Default.DateRange, title = "Smart Schedule", onClick = onScheduleClick)
                HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                ProfileMenuItem(icon = Icons.Default.Notifications, title = "Push Notifications", onClick = { })
                HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                ProfileMenuItem(icon = Icons.Default.Settings, title = "Advanced Settings", onClick = { })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. زر تسجيل الخروج (مدمج كعنصر قائمة منفصل باللون الأحمر)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.logout(onLogoutSuccess) }
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = UrgentRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Log Out", color = UrgentRed, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(text = "SmartMail v1.0.0", color = CardBorder, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 📌 مكون فرعي: عنصر قائمة بسيط وأنيق
@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "›", color = TextSecondary, fontSize = 20.sp)
    }
}