package com.graceon.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.graceon.domain.model.Prescription
import com.graceon.feature.gacha.GachaScreen
import com.graceon.feature.gacha.GachaViewModel
import com.graceon.feature.result.ResultScreen
import com.graceon.feature.result.ResultViewModel
import com.graceon.feature.worry.WorryScreen
import com.graceon.feature.worry.WorryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Navigation Graph for GraceOn App
 */
@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Worry.route
    ) {
        // Worry Selection Screen
        composable(Screen.Worry.route) {
            val viewModel: WorryViewModel = koinViewModel()
            WorryScreen(
                viewModel = viewModel,
                onNavigateToGacha = { categoryId, detailId, customWorry, isAiMode ->
                    val route = Screen.Gacha.createRoute(
                        categoryId = categoryId,
                        detailId = detailId,
                        customWorry = customWorry,
                        isAiMode = isAiMode
                    )
                    navController.navigate(route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Gacha Animation Screen
        composable(
            route = Screen.Gacha.route,
            arguments = listOf(
                navArgument("categoryId") { 
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("detailId") { 
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("customWorry") { 
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("isAiMode") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            val viewModel: GachaViewModel = koinViewModel()
            GachaScreen(
                viewModel = viewModel,
                onNavigateToResult = { prescription, categoryId, detailId, customWorry, isAiMode ->
                    val route = Screen.Result.createRoute(
                        prescription = prescription,
                        categoryId = categoryId,
                        detailId = detailId,
                        customWorry = customWorry,
                        isAiMode = isAiMode
                    )
                    navController.navigate(route) {
                        popUpTo(Screen.Worry.route)
                    }
                }
            )
        }

        // Result Screen
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("prescription") { type = NavType.StringType },
                navArgument("categoryId") { 
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("detailId") { 
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("customWorry") { 
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("isAiMode") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            val viewModel: ResultViewModel = koinViewModel()
            ResultScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    navController.navigate(Screen.Worry.route) {
                        popUpTo(Screen.Worry.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Screen Routes
 */
sealed class Screen(val route: String) {
    data object Worry : Screen("worry")
    
    data object Gacha : Screen(
        "gacha?categoryId={categoryId}&detailId={detailId}&customWorry={customWorry}&isAiMode={isAiMode}"
    ) {
        fun createRoute(
            categoryId: String?,
            detailId: String?,
            customWorry: String?,
            isAiMode: Boolean
        ): String {
            val encodedWorry = customWorry?.let { 
                URLEncoder.encode(it, "UTF-8") 
            }
            return "gacha?categoryId=$categoryId&detailId=$detailId&customWorry=$encodedWorry&isAiMode=$isAiMode"
        }
    }
    
    data object Result : Screen(
        "result?prescription={prescription}&categoryId={categoryId}&detailId={detailId}&customWorry={customWorry}&isAiMode={isAiMode}"
    ) {
        fun createRoute(
            prescription: Prescription,
            categoryId: String?,
            detailId: String?,
            customWorry: String?,
            isAiMode: Boolean
        ): String {
            val prescriptionJson = Json.encodeToString(prescription)
            val encodedPrescription = URLEncoder.encode(prescriptionJson, "UTF-8")
            val encodedWorry = customWorry?.let { 
                URLEncoder.encode(it, "UTF-8") 
            }
            return "result?prescription=$encodedPrescription&categoryId=$categoryId&detailId=$detailId&customWorry=$encodedWorry&isAiMode=$isAiMode"
        }
    }
}
