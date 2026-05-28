package com.example.labcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.labcards.ui.Screen
import com.example.labcards.ui.screens.CalculatorHomeScreen
import com.example.labcards.ui.screens.CardEditScreen
import com.example.labcards.ui.screens.CardRepositoryScreen
import com.example.labcards.ui.screens.DilutionCalcScreen
import com.example.labcards.ui.screens.ExecutionScreen
import com.example.labcards.ui.screens.FlowEditorScreen
import com.example.labcards.ui.screens.FlowListScreen
import com.example.labcards.ui.screens.HomeScreen
import com.example.labcards.ui.screens.MolarityCalcScreen
import com.example.labcards.ui.screens.UnitConverterScreen
import com.example.labcards.ui.theme.LabCardsTheme
import com.example.labcards.ui.viewmodel.FlowViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LabCardsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val flowViewModel: FlowViewModel = viewModel()
                    val summaries by flowViewModel.experimentSummaries.collectAsState()
                    val cardTemplates by flowViewModel.cardTemplates.collectAsState()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToFlows = { navController.navigate(Screen.FlowList.route) },
                                onNavigateToCardRepository = { navController.navigate(Screen.CardRepository.route) },
                                onNavigateToCalculator = { navController.navigate(Screen.CalculatorHome.route) }
                            )
                        }
                        composable(Screen.CardRepository.route) {
                            CardRepositoryScreen(
                                templates = cardTemplates,
                                onCreate = {
                                    flowViewModel.startTemplateCreate()
                                    navController.navigate(Screen.CardEditor.createRoute(null))
                                },
                                onEdit = { templateId ->
                                    flowViewModel.startTemplateEdit(templateId)
                                    navController.navigate(Screen.CardEditor.createRoute(null))
                                },
                                onDelete = flowViewModel::deleteCardTemplate,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.FlowList.route) {
                            FlowListScreen(
                                summaries = summaries,
                                onNavigateToEditor = { id -> navController.navigate(Screen.FlowEditor.createRoute(id)) },
                                onStartExperiment = { id -> navController.navigate(Screen.Execution.createRoute(id)) },
                                onCopy = flowViewModel::copyExperiment,
                                onDelete = flowViewModel::deleteExperiment,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.FlowEditor.route) { backStackEntry ->
                            val templateId = backStackEntry.arguments?.getString("templateId")?.toLongOrNull() ?: 0L
                            LaunchedEffect(templateId) { flowViewModel.openEditor(templateId) }
                            val editorState by flowViewModel.editorState.collectAsState()
                            FlowEditorScreen(
                                state = editorState,
                                onNameChange = flowViewModel::updateExperimentName,
                                onTagsChange = flowViewModel::updateExperimentTags,
                                onAddCard = {
                                    flowViewModel.startCardEdit(null)
                                    navController.navigate(Screen.CardEditor.createRoute(null))
                                },
                                onEditCard = { index ->
                                    flowViewModel.startCardEdit(index)
                                    navController.navigate(Screen.CardEditor.createRoute(index))
                                },
                                onDeleteCard = flowViewModel::removeCard,
                                onMoveCard = flowViewModel::moveCard,
                                savedTemplates = cardTemplates,
                                onUseTemplate = flowViewModel::addTemplateToCurrentFlow,
                                onDeleteTemplate = flowViewModel::deleteCardTemplate,
                                onSave = { flowViewModel.saveExperiment { navController.popBackStack() } },
                                onSaveAs = { flowViewModel.saveExperiment(saveAsNew = true) { navController.popBackStack() } },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.CardEditor.route) {
                            val cardState by flowViewModel.cardEditorState.collectAsState()
                            CardEditScreen(
                                state = cardState,
                                onAddText = flowViewModel::addTextBlock,
                                onAddNumber = flowViewModel::addNumberBlock,
                                onAddTime = flowViewModel::addTimeBlock,
                                onUpdateBlock = flowViewModel::updateBlock,
                                onRemoveBlock = flowViewModel::removeBlock,
                                onStyleChange = flowViewModel::updateCardStyle,
                                onFixedTimerEnabledChange = flowViewModel::setFixedTimerEnabled,
                                onFixedTimerChange = flowViewModel::updateFixedTimer,
                                onSave = {
                                    flowViewModel.commitCardEditor {
                                        navController.popBackStack()
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Execution.route) { backStackEntry ->
                            val templateId = backStackEntry.arguments?.getString("templateId")?.toLongOrNull() ?: 0L
                            val cards by flowViewModel.getCardsForTemplate(templateId).collectAsState()
                            ExecutionScreen(
                                cards = cards,
                                onComplete = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.CalculatorHome.route) {
                            CalculatorHomeScreen(
                                onNavigateToDilution = { navController.navigate(Screen.DilutionCalc.route) },
                                onNavigateToMolarity = { navController.navigate(Screen.MolarityCalc.route) },
                                onNavigateToUnitConverter = { navController.navigate(Screen.UnitConverter.route) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.DilutionCalc.route) {
                            DilutionCalcScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Screen.MolarityCalc.route) {
                            MolarityCalcScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Screen.UnitConverter.route) {
                            UnitConverterScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
