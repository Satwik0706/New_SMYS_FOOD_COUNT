package com.satwik.oodapplication.data.model

data class Menu(
    val date: String = "",
    val breakfast: MealInfo = MealInfo(),
    val lunch: MealInfo = MealInfo(),
    val snack: MealInfo = MealInfo(),
    val dinner: MealInfo = MealInfo()
)

data class MealInfo(
    val items: List<String> = emptyList(),
    val timing: String = ""
)
