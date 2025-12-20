package com.graceon.di

import com.graceon.BuildConfig
import com.graceon.core.common.DefaultDispatcherProvider
import com.graceon.core.common.DispatcherProvider
import com.graceon.core.network.GeminiApiClient
import com.graceon.data.repository.PrescriptionRepositoryImpl
import com.graceon.data.repository.SavedPrescriptionRepositoryImpl
import com.graceon.domain.repository.PrescriptionRepository
import com.graceon.domain.repository.SavedPrescriptionRepository
import com.graceon.domain.usecase.DeletePrescriptionUseCase
import com.graceon.domain.usecase.GeneratePrayerUseCase
import com.graceon.domain.usecase.GeneratePrescriptionUseCase
import com.graceon.domain.usecase.GetSavedPrescriptionsUseCase
import com.graceon.domain.usecase.SavePrescriptionUseCase
import com.graceon.feature.gacha.GachaViewModel
import com.graceon.feature.result.ResultViewModel
import com.graceon.feature.saved.SavedViewModel
import com.graceon.feature.worry.WorryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI Module
 */
val appModule = module {
    
    // Core
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    
    // Network
    single { 
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            println("⚠️ WARNING: GEMINI_API_KEY is empty. Please check local.properties")
        } else {
            println("✅ GEMINI_API_KEY loaded: ${apiKey.take(5)}...")
        }
        
        GeminiApiClient(
            apiKey = apiKey
        ) 
    }
    
    // Repository
    single<PrescriptionRepository> { 
        PrescriptionRepositoryImpl(
            geminiApiClient = get(),
            dispatcherProvider = get()
        ) 
    }
    
    single<SavedPrescriptionRepository> {
        SavedPrescriptionRepositoryImpl(context = androidContext())
    }
    
    // UseCase
    factory { GeneratePrescriptionUseCase(repository = get()) }
    factory { GeneratePrayerUseCase(repository = get()) }
    factory { SavePrescriptionUseCase(repository = get()) }
    factory { GetSavedPrescriptionsUseCase(repository = get()) }
    factory { DeletePrescriptionUseCase(repository = get()) }
    
    // ViewModels
    viewModel { WorryViewModel() }
    viewModel { GachaViewModel(generatePrescriptionUseCase = get(), savedStateHandle = get()) }
    viewModel { ResultViewModel(generatePrayerUseCase = get(), savePrescriptionUseCase = get(), savedStateHandle = get()) }
    viewModel { SavedViewModel(getSavedPrescriptionsUseCase = get(), deletePrescriptionUseCase = get()) }
}
