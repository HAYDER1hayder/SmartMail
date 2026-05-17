package com.example.smartmail.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. نظام الألوان الداكن الفخم الخاص بنا
private val SmartMailDarkColorScheme = darkColorScheme(
    primary = PremiumAccent,
    secondary = TextSecondary,
    tertiary = PremiumAccent,
    background = BackgroundDark,
    surface = CardSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

// 2. السمة (Theme) الأساسية للتطبيق
@Composable
fun SmartMailTheme(
    darkTheme: Boolean = true, // نحن نجبر التطبيق على الـ Dark Mode دائماً للحفاظ على الفخامة
    // Dynamic color متاح في أندرويد 12+، لكننا نلغيه لكي لا يطغى على ألواننا الخاصة
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> SmartMailDarkColorScheme
    }

    // 3. تلوين شريط الحالة (Status Bar) وشريط الأزرار السفلية (Navigation Bar) ليتناسق مع الخلفية الداكنة
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}