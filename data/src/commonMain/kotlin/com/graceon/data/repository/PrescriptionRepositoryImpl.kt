package com.graceon.data.repository

import com.graceon.core.common.DispatcherProvider
import com.graceon.core.common.Result
import com.graceon.core.network.GraceOnProxyApiClient
import com.graceon.domain.model.DailyFreeUsage
import com.graceon.domain.model.Prayer
import com.graceon.domain.model.Prescription
import com.graceon.domain.model.WorryContext
import com.graceon.domain.repository.PrescriptionRepository
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * PrescriptionRepository 구현체 (Data Layer)
 */
class PrescriptionRepositoryImpl(
    private val proxyApiClient: GraceOnProxyApiClient,
    private val dispatcherProvider: DispatcherProvider
) : PrescriptionRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun generatePrescription(worryContext: WorryContext): Result<Prescription> {
        return withContext(dispatcherProvider.io) {
            try {
                val worryText = worryContext.toPromptText()

                val prompt = """
                    User's worry context: "$worryText". 
                    
                    Task:
                    1. Choose a comforting Bible verse (Korean Revised Version) that fits this specific worry. 
                       - **IMPORTANT**: Try to provide a **different verse** each time if possible.
                    2. Write a short, warm, and specific message of encouragement (2-3 sentences) in Korean based on the worry.
                    
                    Format strictly as JSON:
                    {
                        "verse": "Verse text (Reference)",
                        "message": "Comforting message"
                    }
                """.trimIndent()

                val responseText = proxyApiClient.generateContent(prompt)

                // JSON 파싱 (```json 제거)
                val cleanJson = responseText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val prescription = json.decodeFromString<Prescription>(cleanJson)

                // Clean up markdown list characters (+, -, *) if present
                // 사용자의 피드백: 말씀 문구에 +가 계속 붙음 -> 모든 + 제거
                val cleanPrescription = prescription.copy(
                    verse = prescription.verse
                        .replace(Regex("^[\\-*]\\s+"), "")
                        .replace(Regex("\\n[\\-*]\\s+"), "\n")
                        .replace(Regex("\\s+"), " ") // Collapse multiple spaces
                        .trim(),
                    message = prescription.message
                        .replace(Regex("^[\\-*]\\s+"), "")
                        .replace(Regex("\\n[\\-*]\\s+"), "\n")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                )
                
                Result.Success(cleanPrescription)
                
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun generatePrayer(
        worryContext: WorryContext,
        verse: String
    ): Result<Prayer> {
        return withContext(dispatcherProvider.io) {
            try {
                val worryText = worryContext.toPromptText()
                
                val prompt = """
                    Context: The user is worried about "$worryText" and received this verse: "$verse".
                    Task: Write a short, touching prayer (3-4 sentences, Korean) using polite honorifics (하오체/합쇼체 appropriate for prayer) that the user can pray. Start with "하나님 아버지," or similar.
                """.trimIndent()

                val prayerText = proxyApiClient.generateContent(prompt)
                Result.Success(Prayer(prayerText.trim()))
                
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun getDailyFreeUsage(): Result<DailyFreeUsage> {
        return withContext(dispatcherProvider.io) {
            try {
                val status = proxyApiClient.getDailyFreeUsage()
                Result.Success(
                    DailyFreeUsage(
                        dailyLimit = status.dailyLimit,
                        usedToday = status.usedToday,
                        remainingToday = status.remainingToday
                    )
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}
