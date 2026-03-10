package com.graceon

import com.graceon.data.datastore.AuthPreferences
import com.graceon.data.datastore.NotificationPreferences
import com.graceon.data.datastore.ThemePreferences
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.domain.usecase.GrantRewardedCreditUseCase
import com.graceon.domain.usecase.GetDailyFreeUsageUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase

internal data class GraceOnDependencies(
    val authPreferences: AuthPreferences,
    val notificationPreferences: NotificationPreferences,
    val themePreferences: ThemePreferences,
    val generatePrescriptionUseCase: GeneratePrescriptionUseCase,
    val generatePrayerUseCase: GeneratePrayerUseCase,
    val grantRewardedCreditUseCase: GrantRewardedCreditUseCase,
    val getDailyFreeUsageUseCase: GetDailyFreeUsageUseCase,
    val savePrescriptionUseCase: SavePrescriptionUseCase,
    val getSavedPrescriptionsUseCase: GetSavedPrescriptionsUseCase,
    val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    val signInWithEmail: suspend (String, String) -> Unit,
    val signUpWithEmail: suspend (String, String) -> Boolean,
    val resendConfirmationEmail: suspend (String) -> Unit,
    val sendPasswordResetEmail: suspend (String) -> Unit,
    val signInWithGoogle: suspend () -> Unit,
    val logout: suspend () -> Unit
)
