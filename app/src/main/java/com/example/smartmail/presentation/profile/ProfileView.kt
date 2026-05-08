package com.example.smartmail.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
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
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Account", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. الصورة الشخصية عالية الجودة (HD) مع الإطار الأنيق
            Box(contentAlignment = Alignment.BottomEnd) {
                coil.compose.AsyncImage(
                    model = userData?.profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp) // حجم احترافي وليس عملاقاً
                        .clip(CircleShape)
                        .border(3.dp, AccentCyberPink, CircleShape),
                    contentScale = ContentScale.Crop
                )
                // شارة صغيرة خضراء تدل على أن الحساب نشط (Active)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(AccentNeonGreen)
                        .border(4.dp, BackgroundDark, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. اسم المستخدم والإيميل
            Text(text = userData?.username ?: "SmartMail User", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(text = "Premium Member", color = AccentNeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(40.dp))

            // 3. القائمة المعمّرة بالخصائص (Settings List)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardSurfaceDark)
            ) {
                ProfileMenuItem(icon = Icons.Default.Notifications, title = "Push Notifications", subtitle = "On for urgent mails")
                HorizontalDivider(color = BackgroundDark, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                ProfileMenuItem(icon = Icons.Default.Settings, title = "n8n AI Settings", subtitle = "Manage webhook URLs")
                HorizontalDivider(color = BackgroundDark, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                ProfileMenuItem(icon = Icons.Default.Info, title = "Help & Support", subtitle = "Contact development team")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. زر تسجيل الخروج (أنيق ومناسب الحجم)
            OutlinedButton(
                onClick = { viewModel.logout(onLogoutSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = UrgentRed),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, UrgentRed.copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 📌 مكون فرعي: عنصر واحد من القائمة
@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Open setting */ }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = BackgroundDark,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = TextSecondary, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = TextSecondary, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "›", color = TextSecondary, fontSize = 20.sp) // سهم صغير لليمين
    }
}