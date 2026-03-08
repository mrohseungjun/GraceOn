package com.graceon

import android.content.Context
import com.graceon.core.network.AndroidSupabaseSessionStore
import com.graceon.core.common.DefaultDispatcherProvider
import com.graceon.core.network.GraceOnProxyApiClient
import com.graceon.data.datastore.OnboardingPreferences
import com.graceon.data.datastore.NotificationPreferences
import com.graceon.data.datastore.ThemePreferences
import com.graceon.data.repository.PlatformContext
import com.graceon.data.repository.PrescriptionRepositoryImpl
import com.graceon.data.repository.SavedPrescriptionRepositoryImpl
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.domain.usecase.GetDailyFreeUsageUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase

internal fun createGraceOnAndroidDependencies(
    context: Context,
    apiBaseUrl: String,
    supabaseAnonKey: String
): GraceOnDependencies {
    val dispatcherProvider = DefaultDispatcherProvider()
    val proxyApiClient = GraceOnProxyApiClient(
        baseUrl = apiBaseUrl,
        supabaseAnonKey = supabaseAnonKey,
        sessionStore = AndroidSupabaseSessionStore(context)
    )
    val prescriptionRepository = PrescriptionRepositoryImpl(
        proxyApiClient = proxyApiClient,
        dispatcherProvider = dispatcherProvider
    )
    val savedPrescriptionRepository = SavedPrescriptionRepositoryImpl(
        platformContext = PlatformContext(context)
    )

    return GraceOnDependencies(
        onboardingPreferences = OnboardingPreferences(PlatformContext(context)),
        notificationPreferences = NotificationPreferences(PlatformContext(context)),
        themePreferences = ThemePreferences(PlatformContext(context)),
        generatePrescriptionUseCase = GeneratePrescriptionUseCase(prescriptionRepository),
        generatePrayerUseCase = GeneratePrayerUseCase(prescriptionRepository),
        getDailyFreeUsageUseCase = GetDailyFreeUsageUseCase(prescriptionRepository),
        savePrescriptionUseCase = SavePrescriptionUseCase(savedPrescriptionRepository),
        getSavedPrescriptionsUseCase = GetSavedPrescriptionsUseCase(savedPrescriptionRepository),
        deletePrescriptionUseCase = DeletePrescriptionUseCase(savedPrescriptionRepository)
    )
}
