package com.graceon

import com.graceon.core.common.DefaultDispatcherProvider
import com.graceon.core.common.Result
import com.graceon.core.network.AppUpdateConfigResponse
import com.graceon.core.network.GraceOnProxyApiClient
import com.graceon.core.network.IosSupabaseSessionStore
import com.graceon.data.datastore.AuthPreferences
import com.graceon.data.datastore.NotificationPreferences
import com.graceon.data.datastore.ThemePreferences
import com.graceon.data.repository.PlatformContext
import com.graceon.data.repository.PrescriptionRepositoryImpl
import com.graceon.data.repository.SavedPrescriptionRepositoryImpl
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.domain.usecase.GrantRewardedCreditUseCase
import com.graceon.domain.usecase.GetDailyFreeUsageUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase
import com.graceon.update.AppPlatform
import com.graceon.update.AppUpdateConfig

internal fun createGraceOnIosDependencies(
    apiBaseUrl: String,
    supabaseAnonKey: String,
    openUrl: (String) -> Unit
): GraceOnDependencies {
    val dispatcherProvider = DefaultDispatcherProvider()
    val proxyApiClient = GraceOnProxyApiClient(
        baseUrl = apiBaseUrl,
        supabaseAnonKey = supabaseAnonKey,
        sessionStore = IosSupabaseSessionStore()
    )
    val prescriptionRepository = PrescriptionRepositoryImpl(
        proxyApiClient = proxyApiClient,
        dispatcherProvider = dispatcherProvider
    )
    val savedPrescriptionRepository = SavedPrescriptionRepositoryImpl(
        platformContext = PlatformContext()
    )

    return GraceOnDependencies(
        authPreferences = AuthPreferences(PlatformContext()),
        notificationPreferences = NotificationPreferences(PlatformContext()),
        themePreferences = ThemePreferences(PlatformContext()),
        generatePrescriptionUseCase = GeneratePrescriptionUseCase(prescriptionRepository),
        generatePrayerUseCase = GeneratePrayerUseCase(prescriptionRepository),
        grantRewardedCreditUseCase = GrantRewardedCreditUseCase(prescriptionRepository),
        getDailyFreeUsageUseCase = GetDailyFreeUsageUseCase(prescriptionRepository),
        savePrescriptionUseCase = SavePrescriptionUseCase(savedPrescriptionRepository),
        getSavedPrescriptionsUseCase = GetSavedPrescriptionsUseCase(savedPrescriptionRepository),
        deletePrescriptionUseCase = DeletePrescriptionUseCase(savedPrescriptionRepository),
        signInWithEmail = { email, password ->
            proxyApiClient.signInWithEmail(email, password)
        },
        signUpWithEmail = { email, password ->
            proxyApiClient.signUpWithEmail(email, password)
        },
        resendConfirmationEmail = { email ->
            proxyApiClient.resendConfirmationEmail(email)
        },
        sendPasswordResetEmail = { email ->
            proxyApiClient.sendPasswordResetEmail(email)
        },
        signInWithGoogle = {
            proxyApiClient.signInWithGoogle(openUrl)
        },
        getCurrentUserEmail = {
            proxyApiClient.getCurrentUserEmail()
        },
        getAppUpdateConfig = { platform ->
            runCatching {
                proxyApiClient
                    .getAppUpdateConfig(platform.toApiValue())
                    .toUpdateConfig()
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(it) }
            )
        },
        logout = {
            proxyApiClient.logout()
            AuthPreferences(PlatformContext()).resetAuthenticated()
        }
    )
}

private fun AppPlatform.toApiValue(): String = when (this) {
    AppPlatform.Android -> "android"
    AppPlatform.Ios -> "ios"
}

private fun AppUpdateConfigResponse.toUpdateConfig(): AppUpdateConfig =
    AppUpdateConfig(
        latestVersion = latestVersion,
        minimumSupportedVersion = minimumSupportedVersion,
        optionalTitle = optionalTitle,
        optionalMessage = optionalMessage,
        requiredTitle = requiredTitle,
        requiredMessage = requiredMessage
    )
