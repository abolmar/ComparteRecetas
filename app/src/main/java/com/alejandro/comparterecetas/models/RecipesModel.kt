package com.alejandro.comparterecetas.models

import java.math.BigInteger

data class RecipesModel (
    var id: String = "",
    var name: String = "",
    var userId: String = "",
    var preparation: String = "",
    var timeH: Int = 0,
    var timeM: Int = 0,
    var positive: Int = 0,
    var negative: Int = 0,
    var category: String = "",
    var type: Int = 0,
    var date: String = "",
    var remove: Int = 0
)
