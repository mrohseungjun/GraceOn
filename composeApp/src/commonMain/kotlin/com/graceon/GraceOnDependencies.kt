package com.graceon

import com.graceon.data.datastore.OnboardingPreferences
import com.graceon.data.datastore.NotificationPreferences
import com.graceon.data.datastore.ThemePreferences
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase

internal data class GraceOnDependencies(
    val onboardingPreferences: OnboardingPreferences,
    val notificationPreferences: NotificationPreferences,
    val themePreferences: ThemePreferences,
    val generatePrescriptionUseCase: GeneratePrescriptionUseCase,
    val generatePrayerUseCase: GeneratePrayerUseCase,
    val savePrescriptionUseCase: SavePrescriptionUseCase,
    val getSavedPrescriptionsUseCase: GetSavedPrescriptionsUseCase,
    val deletePrescriptionUseCase: DeletePrescriptionUseCase
)
