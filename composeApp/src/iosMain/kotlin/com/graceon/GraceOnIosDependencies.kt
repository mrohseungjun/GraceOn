package com.graceon

import com.graceon.core.common.DefaultDispatcherProvider
import com.graceon.core.network.GeminiApiClient
import com.graceon.data.datastore.OnboardingPreferences
import com.graceon.data.datastore.NotificationPreferences
import com.graceon.data.datastore.ThemePreferences
import com.graceon.data.repository.PlatformContext
import com.graceon.data.repository.PrescriptionRepositoryImpl
import com.graceon.data.repository.SavedPrescriptionRepositoryImpl
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase

internal fun createGraceOnIosDependencies(apiKey: String): GraceOnDependencies {
    val dispatcherProvider = DefaultDispatcherProvider()
    val geminiApiClient = GeminiApiClient(apiKey = apiKey)
    val prescriptionRepository = PrescriptionRepositoryImpl(
        geminiApiClient = geminiApiClient,
        dispatcherProvider = dispatcherProvider
    )
    val savedPrescriptionRepository = SavedPrescriptionRepositoryImpl(
        platformContext = PlatformContext()
    )

    return GraceOnDependencies(
        onboardingPreferences = OnboardingPreferences(PlatformContext()),
        notificationPreferences = NotificationPreferences(PlatformContext()),
        themePreferences = ThemePreferences(PlatformContext()),
        generatePrescriptionUseCase = GeneratePrescriptionUseCase(prescriptionRepository),
        generatePrayerUseCase = GeneratePrayerUseCase(prescriptionRepository),
        savePrescriptionUseCase = SavePrescriptionUseCase(savedPrescriptionRepository),
        getSavedPrescriptionsUseCase = GetSavedPrescriptionsUseCase(savedPrescriptionRepository),
        deletePrescriptionUseCase = DeletePrescriptionUseCase(savedPrescriptionRepository)
    )
}
