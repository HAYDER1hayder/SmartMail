package com.example.smartmail.domain.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

// هذه الأداة مسؤولة عن فتح شاشة جوجل، وجلب التوكن، ثم تسجيل الدخول في Firebase
class GoogleAuthClient(
    private val context: Context,
    private val webClientClientId: String , // 👈 هذا الكود سنأخذه من Firebase لاحقاً
    private val repository: MailRepository
) {
    private val auth = Firebase.auth
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    // 1. هذه الدالة تجهز الشاشة المنبثقة لحسابات جوجل
    suspend fun signIn(): IntentSender? {
        val result = try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(webClientClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                // 👈 أضف هذا الجزء لكي لا يكون serverAuthCode دائماً null
                .setServerAuthCodeRequestOptions(
                    BeginSignInRequest.ServerAuthCodeRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(webClientClientId)
                        .setForceCodeForRefreshToken(true)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()

            oneTapClient.beginSignIn(signInRequest).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    // 2. هذه الدالة تأخذ النتيجة من الشاشة المنبثقة وتدخل بها إلى Firebase
    suspend fun signInWithIntent(intent: Intent): String? {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val serverAuthCode = credential.serverAuthCode

        return try {
            val googleIdToken = credential.googleIdToken
            val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(googleCredentials).await()
            val user = authResult.user

            if (user != null && serverAuthCode != null) {
                repository.sendCodeToBackend(serverAuthCode, user.uid)
            }

            user?.uid
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSignedInUser(): UserData? {
        val user = auth.currentUser
        return if (user != null) {
            UserData(
                userId = user.uid,
                username = user.displayName,
                profilePictureUrl = user.photoUrl?.toString()
            )
        } else null
    }
}

// أضف هذا الـ Data Class خارج كلاس GoogleAuthClient في أسفل الملف
data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)


