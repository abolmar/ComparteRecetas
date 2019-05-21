package com.alejandro.comparterecetas.models

data class VotesModel (
    var id: String = "",
    var recipeId: String = "",
    var userId: String = "",
    var votePositive: Int = 0,
    var voteNegative: Int = 0,
    var date: String = ""
)