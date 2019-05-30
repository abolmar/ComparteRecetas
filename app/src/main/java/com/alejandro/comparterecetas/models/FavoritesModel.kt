package com.alejandro.comparterecetas.models

data class FavoritesModel (
    var id: String = "",
    var recipeId: String = "",
    var userId: String = "",
    var type: String = "",
    var favorite: Int = 0,
    var date: String = ""
)