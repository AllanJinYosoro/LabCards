package com.example.labcards.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FlowList : Screen("flow_list")
    object FlowEditor : Screen("flow_editor/{templateId}") {
        fun createRoute(templateId: Long) = "flow_editor/$templateId"
    }
    object CardEditor : Screen("card_editor/{cardIndex}") {
        fun createRoute(cardIndex: Int?) = "card_editor/${cardIndex ?: -1}"
    }
    object Execution : Screen("execution/{templateId}") {
        fun createRoute(templateId: Long) = "execution/$templateId"
    }
    object CalculatorHome : Screen("calculator_home")
    object DilutionCalc : Screen("dilution_calc")
    object MolarityCalc : Screen("molarity_calc")
    object UnitConverter : Screen("unit_converter")
}
