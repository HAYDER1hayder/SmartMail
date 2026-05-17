package com.example.smartmail.presentation.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartmail.ui.theme.PremiumAccent
import com.example.smartmail.ui.theme.BackgroundDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingView(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit

) {

    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            scope.launch {
                // نرسل النتيجة لـ Firebase ليتحقق منها
                val userId = viewModel.googleAuthClient.signInWithIntent(result.data ?: return@launch)
                if (userId != null) {
                    viewModel.saveUserSession()

                    onSignInSuccess()
                }
            }
        }
    }

    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "Alpha Animation"
    )

    LaunchedEffect(key1 = true) {
        delay(100)
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // 1. اللوجو (أيقونة إيميل مع تدرج لوني)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B), BackgroundDark)
                        )
                    )
                    .border(2.dp, PremiumAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp),
                    tint = PremiumAccent
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 2. اسم التطبيق
            Text(
                text = "SmartMail",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. الوصف
            Text(
                text = "Your inbox, powered by AI.\nFilter the noise, focus on what matters.",
                color = Color(0xFF94A3B8),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // 4. زر تسجيل الدخول بـ Google
            Button(
                onClick = {
                    scope.launch {
                        // عند الضغط، نطلب من أداتنا تجهيز شاشة حسابات جوجل
                        val intentSender = viewModel.googleAuthClient.signIn()
                        if (intentSender != null) {
                            // إذا نجح التجهيز، نعرض الشاشة للمستخدم
                            launcher.launch(
                                androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // زر جوجل دائماً أبيض
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // حرف G الملون (بديل لأيقونة جوجل حتى نحملها لاحقاً)
                    Text(text = "G", color = Color(0xFFDB4437), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // نص صغير لحقوق الخصوصية
            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy.",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}