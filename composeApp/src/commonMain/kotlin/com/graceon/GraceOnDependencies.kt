package com.graceon

import com.graceon.data.datastore.OnboardingPreferences
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase

internal data class GraceOnDependencies(
    val onboardingPreferences: OnboardingPreferences,
    val generatePrescriptionUseCase: GeneratePrescriptionUseCase,
    val generatePrayerUseCase: GeneratePrayerUseCase,
    val savePrescriptionUseCase: SavePrescriptionUseCase,
    val getSavedPrescriptionsUseCase: GetSavedPrescriptionsUseCase,
    val deletePrescriptionUseCase: DeletePrescriptionUseCase
)
