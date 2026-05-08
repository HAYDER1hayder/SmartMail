package com.example.smartmail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartmail.domain.preferences.SessionManager
import com.example.smartmail.presentation.home.HomeView
import com.example.smartmail.presentation.onboarding.OnboardingView
import com.example.smartmail.presentation.profile.ProfileView
import com.example.smartmail.ui.theme.SmartMailTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // نحقن مدير الذاكرة لنعرف حالة المستخدم فور تشغيل التطبيق
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartMailTheme {
                // مدير التنقل بين الشاشات
                val navController = rememberNavController()

                // نقرأ حالة الدخول من الذاكرة (سريعة جداً لأنها DataStore)
                val isLoggedIn by sessionManager.getLoginState().collectAsState(initial = null)

                // ننتظر أجزاء من الثانية حتى يتأكد التطبيق من الذاكرة
                if (isLoggedIn != null) {

                    // 🧠 الذكاء هنا: تحديد شاشة البداية ديناميكياً
                    val startDestination = if (isLoggedIn == true) "home" else "onboarding"

                    // رسم خريطة الشاشات
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {

                        // 1. شاشة الترحيب وتسجيل الدخول
                        composable("onboarding") {
                            OnboardingView(
                                onSignInSuccess = {
                                    // إذا نجح الدخول، انتقل للرئيسية وامسح شاشة الترحيب من الذاكرة
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. الشاشة الرئيسية (البطاقات الفخمة)
                        composable("home") {
                            HomeView(
                                onProfileClick = { navController.navigate("profile") },
                            )
                        }
                        composable("profile") {
                            ProfileView(
                                onBackClick = { navController.popBackStack() },
                                onLogoutSuccess = {

                                    navController.navigate("onboarding") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartMailTheme {
        Greeting("Android")
    }
}