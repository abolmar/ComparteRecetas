package com.alejandro.comparterecetas.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.alejandro.comparterecetas.models.*
import com.alejandro.comparterecetas.models.*

class DataBaseHandler(context: Context) : SQLiteOpenHelper(context, "DataBaseRecipes.db", null, 1) {

    companion object {
//        private const val tableUsers = "users"
        private const val tableUsersLogin = "usersLogin"
        private const val tableRecipes = "recipes"
        private const val tableIngredients = "ingredients"
        private const val tableImages = "images"
        private const val tableFavorites = "favorites"
        private const val tableUsersVotes = "usersVotes"

//        //  Columnas tabla "users"
//        private const val usersId = "id"
//        private const val usersEmail = "email"
//        private const val usersName = "name"
//        private const val usersPasswd = "passwd"
//        private const val usersImage = "imagePath"

        //  Columnas tabla "usersLogin"
        private const val usersLoginId = "id"
        private const val usersLoginEmail = "email"
        private const val usersLoginName = "name"
        private const val usersLoginPasswd = "passwd"
        private const val usersLoginImage = "imagePath"
        private const val usersLoginImageName = "imageName"
        private const val usersLoginLogin = "login"

        //  Columnas tabla "recipes"
        private const val recipeId = "id"
        private const val recipeName = "name"
        private const val recipeUserId = "userId"
        private const val recipePreparation = "preparation"
        private const val recipeTimeHours = "timeH"
        private const val recipeTimeMinutes = "timeM"
        private const val recipePositiveVote = "positive"
        private const val recipeNegativeVote = "negative"
        private const val recipeCategory = "category"
        private const val recipeType = "type"
        private const val recipeDate = "date"
        private const val recipeRemove = "remove"

        //  Columnas tabla "ingredients"
        private const val ingredientId = "id"
        private const val ingredientRecipeId = "recipeId"
        private const val ingredientName = "name"
        private const val ingredientDate = "date"

        //  Columnas tabla "images"
        private const val imageId = "id"
        private const val imageRecipeId = "recipeId"
        private const val imagePath = "path"
        private const val imageName = "name"
        private const val imageDate = "date"

        //  Columnas tabla "favorites"
        private const val favoriteId = "id"
        private const val favoriteRecipeId = "recipeId"
        private const val favoriteUserId = "userId"
        private const val favoriteType = "type"
        private const val favoriteBoolean = "favorite"
        private const val favoriteDate = "date"

        //  Columnas tabla "usersVotes"
        private const val voteId = "id"
        private const val voteRecipeId = "recipeId"
        private const val voteUserId = "userId"
        private const val votePositive = "positive"
        private const val voteNegative = "negative"
        private const val voteDate = "date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
//        val createTableUsers = "CREATE TABLE IF NOT EXISTS `$tableUsers` (" +
//                "`$usersId` Integer, " +
//                "`$usersEmail` TEXT, " +
//                "`$usersName` TEXT, " +
//                "`$usersPasswd` TEXT, " +
//                "`$usersImage` TEXT, " +
//                "PRIMARY KEY(`$usersId`)" +
//                ");"
//        db?.execSQL(createTableUsers)

        val createTableUsersLogin = "CREATE TABLE IF NOT EXISTS `$tableUsersLogin` (" +
                "`$usersLoginId` TEXT, " +
                "`$usersLoginEmail` TEXT, " +
                "`$usersLoginName` TEXT, " +
                "`$usersLoginPasswd` TEXT, " +
                "`$usersLoginImage` TEXT, " +
                "`$usersLoginImageName` TEXT, " +
                "`$usersLoginLogin` Integer, " +
                "PRIMARY KEY(`$usersLoginId`)" +
                ");"
        db?.execSQL(createTableUsersLogin)

        val createTableRecipes = "CREATE TABLE IF NOT EXISTS `$tableRecipes` (" +
                "`$recipeId` TEXT, " +
                "`$recipeName` TEXT, " +
                "`$recipeUserId` TEXT, " +
                "`$recipePreparation` TEXT, " +
                "`$recipeTimeHours` Integer DEFAULT 0, " +
                "`$recipeTimeMinutes` Integer DEFAULT 0, " +
                "`$recipePositiveVote` Integer DEFAULT 0, " +
                "`$recipeNegativeVote` Integer DEFAULT 0, " +
                "`$recipeCategory` TEXT, " +
                "`$recipeType` Integer DEFAULT 0, " +
                "`$recipeDate` TEXT, " +
                "`$recipeRemove` Integer DEFAULT 0, " +
                "PRIMARY KEY (`$recipeId`), " +
                "FOREIGN KEY(`$recipeUserId`) REFERENCES `$tableUsersLogin`(`$usersLoginId`)" +
                ");"
        db?.execSQL(createTableRecipes)

        val createTableIngredients = "CREATE TABLE IF NOT EXISTS `$tableIngredients` (" +
                "`$ingredientId` TEXT, " +
                "`$ingredientRecipeId` TEXT, " +
                "`$ingredientName` TEXT, " +
                "`$ingredientDate` TEXT, " +
                "PRIMARY KEY (`$ingredientId`), " +
                "FOREIGN KEY(`$ingredientRecipeId`) REFERENCES `$tableRecipes`(`$recipeId`)" +
                ");"
        db?.execSQL(createTableIngredients)

        val createTableImages = "CREATE TABLE IF NOT EXISTS `$tableImages` (" +
                "`$imageId` TEXT, " +
                "`$imageRecipeId` TEXT, " +
                "`$imagePath` TEXT, " +
                "`$imageName` TEXT, " +
                "`$imageDate` TEXT, " +
                "PRIMARY KEY (`$imageId`), " +
                "FOREIGN KEY(`$imageRecipeId`) REFERENCES `$tableRecipes`(`$recipeId`)" +
                ");"
        db?.execSQL(createTableImages)

        val createTableFavorites = "CREATE TABLE IF NOT EXISTS `$tableFavorites` (" +
                "`$favoriteId` TEXT, " +
                "`$favoriteRecipeId` TEXT, " +
                "`$favoriteUserId` TEXT, " +
                "`$favoriteType` TEXT, " +
                "`$favoriteBoolean` Integer, " +
                "`$favoriteDate` TEXT, " +
                "PRIMARY KEY (`$favoriteId`), " +
                "FOREIGN KEY(`$favoriteRecipeId`) REFERENCES `$tableRecipes`(`$recipeId`)" +
                ");"
        db?.execSQL(createTableFavorites)

        val createTableUsersVotes = "CREATE TABLE IF NOT EXISTS `$tableUsersVotes` (" +
                "`$voteId` TEXT, " +
                "`$voteRecipeId` TEXT, " +
                "`$voteUserId` TEXT, " +
                "`$votePositive` Integer DEFAULT 0, " +
                "`$voteNegative` Integer DEFAULT 0, " +
                "`$voteDate` TEXT, " +
                "PRIMARY KEY (`$voteId`), " +
                "FOREIGN KEY(`$voteRecipeId`) REFERENCES `$tableRecipes`(`$recipeId`), " +
                "FOREIGN KEY(`$voteUserId`) REFERENCES `$tableUsersLogin`(`$usersLoginId`)" +
                ");"
        db?.execSQL(createTableUsersVotes)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

//    //  Inserta datos en la tabla "users"
//    fun addUserTableUser(usersModel: UsersModel): Boolean {
//        val db = writableDatabase
//        val values = ContentValues()
//
//        values.put(usersEmail, usersModel.email)
//        values.put(usersName, usersModel.name)
//        values.put(usersPasswd, usersModel.passwd)
//        values.put(usersImage, usersModel.imagePath)
//
//        val success = db.insert(tableUsers, null, values)
//
//        db.close()
//
//        return (Integer.parseInt("$success") != -1)
//    }

    //  Inserta datos en la tabla "usersLogin"
    fun addUserTableUserLogin(usersModel: UsersModel): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        values.put(usersLoginId, usersModel.id)
        values.put(usersLoginEmail, usersModel.email)
        values.put(usersLoginName, usersModel.name)
        values.put(usersLoginPasswd, usersModel.passwd)
        values.put(usersLoginLogin, usersModel.login)

        val success = db.insert(tableUsersLogin, null, values)

        db.close()

        return (Integer.parseInt("$success") != -1)
    }

    //  Insertar datos en la tabla "recipes"
    fun addRecipe(recipesModel: RecipesModel): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        values.put(recipeId, recipesModel.id)
        values.put(recipeName, recipesModel.name)
        values.put(recipeUserId, recipesModel.userId)
        values.put(recipePreparation, recipesModel.preparation)
        values.put(recipeTimeHours, recipesModel.timeH)
        values.put(recipeTimeMinutes, recipesModel.timeM)
        values.put(recipePositiveVote, recipesModel.positive)
        values.put(recipeNegativeVote, recipesModel.negative)
        values.put(recipeCategory, recipesModel.category)
        values.put(recipeType, recipesModel.type)
        values.put(recipeDate, recipesModel.date)
        values.put(recipeRemove, recipesModel.remove)

        val success = db.insert(tableRecipes, null, values)

        db.close()

        return (Integer.parseInt("$success") != -1)
    }

    //  Inserta datos en la tabla "ingredients"
    fun addIngredientTableIngredients(ingredientsModel: IngredientsModel) {
        val db = writableDatabase
        val values = ContentValues()

        values.put(ingredientId, ingredientsModel.id)
        values.put(ingredientRecipeId, ingredientsModel.idRecipe)
        values.put(ingredientName, ingredientsModel.ingredientName)
        values.put(ingredientDate, ingredientsModel.date)

        db.insert(tableIngredients, null, values)

        db.close()
    }

    //  Inserta datos en la tabla "images"
    fun addImageTableImages(imagesModel: ImagesModel) {
        val db = writableDatabase
        val values = ContentValues()

        values.put(imageId, imagesModel.id)
        values.put(imageRecipeId, imagesModel.recipeId)
        values.put(imagePath, imagesModel.imagePath)
        values.put(imageName, imagesModel.name)
        values.put(imageDate, imagesModel.date)

        db.insert(tableImages, null, values)

        db.close()
    }

    //  Inserta datos en la tabla "usersVotes"
    fun addUserVoteTableUserVotes(votesModel: VotesModel): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        values.put(voteId, votesModel.id)
        values.put(voteRecipeId, votesModel.recipeId)
        values.put(voteUserId, votesModel.userId)
        values.put(votePositive, votesModel.votePositive)
        values.put(voteNegative, votesModel.voteNegative)
        values.put(voteDate, votesModel.date)

        val success = db.insert(tableUsersVotes, null, values)

        db.close()

        return (Integer.parseInt("$success") != -1)
    }

    //  Inserta datos en la tabla "favorites"
    fun addFavoriteRecipe(favoritesModel: FavoritesModel): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        values.put(favoriteId, favoritesModel.id)
        values.put(favoriteRecipeId, favoritesModel.recipeId)
        values.put(favoriteUserId, favoritesModel.userId)
        values.put(favoriteType, favoritesModel.type)
        values.put(favoriteBoolean, favoritesModel.favorite)
        values.put(favoriteDate, favoritesModel.date)

        val success = db.insert(tableFavorites, null, values)

        db.close()

        return (Integer.parseInt("$success") != -1)
    }


//    //  Comprueba que el usuario exista
//    fun getUserEmailFromTableUsers(email:String, passwd:String): Boolean {
//        var user = ""
//        val db = readableDatabase
//        val selectQuery = "SELECT $usersLoginEmail FROM $tableUsersLogin WHERE $usersLoginEmail = '$email' AND $usersLoginPasswd = '$passwd'"
//        val cursor = db.rawQuery(selectQuery, null)
//
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    val eMail = cursor.getString(cursor.getColumnIndex(usersLoginEmail))
//                    user = eMail
//
//                } while (cursor.moveToNext())
//            }
//        }
//        cursor.close()
//        db.close()
//
//        return user != ""
//    }
//
//    //  Comprueba que el email del usuario sea unico
//    fun getEmailFromTableUsers(email:String): Boolean {
//        var user = ""
//        val db = readableDatabase
//        val selectQuery = "SELECT $usersEmail FROM $tableUsers WHERE $usersEmail = '$email'"
//        val cursor = db.rawQuery(selectQuery, null)
//
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    val eMail = cursor.getString(cursor.getColumnIndex(usersEmail))
//                    user = eMail
//
//                } while (cursor.moveToNext())
//            }
//        }
//        cursor.close()
//        db.close()
//
//        return user == ""
//    }

    //  Comprueba si el usuario tiene la sesion iniciada (1)
    fun getLoginTableUserLogin(): Boolean{
        var user = 0
        val db = readableDatabase
        val selectQuery = "SELECT $usersLoginLogin FROM $tableUsersLogin"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getInt(cursor.getColumnIndex(usersLoginLogin))
                    user = data

                    if (user == 1) return true

                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return user != 0
    }

    //  Cambia el login del usuario de 0(sesion cerrada) a 1(iniciar sesion)
    fun updateLoginTableUserLogin(email:String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put(usersLoginLogin, 1)

        db.update(tableUsersLogin, values, "$usersLoginEmail = '$email'", null)
        db.close()
    }

    //  Cambia el login del usuario de 1(sesion iniciada) a 0(cerrar sesion)
    fun updateLogoutTableUserLogin(){
        val db = writableDatabase
        val values = ContentValues()

        values.put(usersLoginLogin, 0)

        db.update(tableUsersLogin, values, "$usersLoginLogin = 1", null)
        db.close()
    }

    //  Retorna el nombre del usuario registrado/logueado
    fun getUserName(): String{
        var name = ""
        val db = readableDatabase
        val selectQuery = "SELECT $usersLoginName FROM $tableUsersLogin WHERE $usersLoginLogin = 1"
//        val selectQuery = "SELECT U.$usersName FROM $tableUsers U INNER JOIN $tableUsersLogin UL ON U.$usersEmail = UL.$usersLoginEmail WHERE UL.$usersLoginLogin = 1"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getString(cursor.getColumnIndex(usersLoginName))
                    name = data

                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return name
    }

    //  Retorna el id del usuario registrado/logueado
    fun getUserId(): String{
        var id = ""
        val db = readableDatabase
        val selectQuery = "SELECT $usersLoginId FROM $tableUsersLogin WHERE $usersLoginLogin = 1"
//        val selectQuery = "SELECT U.$usersId FROM $tableUsers U INNER JOIN $tableUsersLogin UL ON U.$usersEmail = UL.$usersLoginEmail WHERE UL.$usersLoginLogin = 1"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getString(cursor.getColumnIndex(usersLoginId))
                    id = data
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return id
    }

    //  Retorna el id de la receta
    fun getRecipeId(userid: String, recipename: String): String {
        var id = ""
        val db = readableDatabase
        val selectQuery = "SELECT $recipeId FROM $tableRecipes WHERE $recipeUserId = '$userid' AND $recipeName = '$recipename'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getString(cursor.getColumnIndex(recipeId))
                    id = data
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return id
    }

    //  Retorna todas las recetas de un usuario que no han sido "eliminadas" (remove = 0)
    fun getAllMyRecipes(userid: String): ArrayList<RecipesModel> {
        val data: ArrayList<RecipesModel> = ArrayList()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $tableRecipes WHERE $recipeUserId = '$userid' AND $recipeRemove = 0 ORDER BY $recipeDate DESC"
        val cursor = db.rawQuery(selectALLQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    val recipe = RecipesModel(

                        cursor.getString(cursor.getColumnIndex(recipeId)),
                        cursor.getString(cursor.getColumnIndex(recipeName)),
                        cursor.getString(cursor.getColumnIndex(recipeUserId)),
                        cursor.getString(cursor.getColumnIndex(recipePreparation)),
                        cursor.getInt(cursor.getColumnIndex(recipeTimeHours)),
                        cursor.getInt(cursor.getColumnIndex(recipeTimeMinutes)),
                        cursor.getInt(cursor.getColumnIndex(recipePositiveVote)),
                        cursor.getInt(cursor.getColumnIndex(recipeNegativeVote)),
                        cursor.getString(cursor.getColumnIndex(recipeCategory)),
                        cursor.getInt(cursor.getColumnIndex(recipeType)),
                        cursor.getString(cursor.getColumnIndex(recipeDate)),
                        cursor.getInt(cursor.getColumnIndex(recipeRemove))
                    )

                    data.add(recipe)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return data
    }

    //  Retorna todas las recetas de un usuario que han sido "eliminadas" (remove = 1)
    fun getAllMyRecipesRemoved(userid: String): ArrayList<RecipesModel> {
        val data: ArrayList<RecipesModel> = ArrayList()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $tableRecipes WHERE $recipeUserId = '$userid' AND $recipeRemove = 1"
        val cursor = db.rawQuery(selectALLQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    val recipe = RecipesModel(

                        cursor.getString(cursor.getColumnIndex(recipeId)),
                        cursor.getString(cursor.getColumnIndex(recipeName)),
                        cursor.getString(cursor.getColumnIndex(recipeUserId)),
                        cursor.getString(cursor.getColumnIndex(recipePreparation)),
                        cursor.getInt(cursor.getColumnIndex(recipeTimeHours)),
                        cursor.getInt(cursor.getColumnIndex(recipeTimeMinutes)),
                        cursor.getInt(cursor.getColumnIndex(recipePositiveVote)),
                        cursor.getInt(cursor.getColumnIndex(recipeNegativeVote)),
                        cursor.getString(cursor.getColumnIndex(recipeCategory)),
                        cursor.getInt(cursor.getColumnIndex(recipeType)),
                        cursor.getString(cursor.getColumnIndex(recipeDate)),
                        cursor.getInt(cursor.getColumnIndex(recipeRemove))
                    )

                    data.add(recipe)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return data
    }

    //  Retorna todas las recetas de todos los usuarios cuyo tipo de receta sea pÚblico(1)
    fun getAllUsersRecipes(): ArrayList<RecipesModel> {
        val data: ArrayList<RecipesModel> = ArrayList()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $tableRecipes WHERE $recipeType = 1 AND $recipeRemove = 0 ORDER BY '$recipeId' DESC"
        val cursor = db.rawQuery(selectALLQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    val recipe = RecipesModel(
                        cursor.getString(cursor.getColumnIndex(recipeId)),
                        cursor.getString(cursor.getColumnIndex(recipeName)),
                        cursor.getString(cursor.getColumnIndex(recipeUserId)),
                        cursor.getString(cursor.getColumnIndex(recipePreparation)),
                        cursor.getInt(cursor.getColumnIndex(recipeTimeHours)),
                        cursor.getInt(cursor.getColumnIndex(recipeTimeMinutes)),
                        cursor.getInt(cursor.getColumnIndex(recipePositiveVote)),
                        cursor.getInt(cursor.getColumnIndex(recipeNegativeVote)),
                        cursor.getString(cursor.getColumnIndex(recipeCategory)),
                        cursor.getInt(cursor.getColumnIndex(recipeType)),
                        cursor.getString(cursor.getColumnIndex(recipeDate)),
                        cursor.getInt(cursor.getColumnIndex(recipeRemove))
                    )

                    data.add(recipe)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return data
    }

    //  Retorna todas los ingredientes de una receta
    fun getAllMyIngredients(recipeid: String?): ArrayList<IngredientsModel> {
        val data: ArrayList<IngredientsModel> = ArrayList()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $tableIngredients WHERE $ingredientRecipeId = '$recipeid'"
        val cursor = db.rawQuery(selectALLQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    val ingredients = IngredientsModel(

                        cursor.getString(cursor.getColumnIndex(ingredientId)),
                        cursor.getString(cursor.getColumnIndex(ingredientRecipeId)),
                        cursor.getString(cursor.getColumnIndex(ingredientName)),
                        cursor.getString(cursor.getColumnIndex(ingredientDate))
                    )

                    data.add(ingredients)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return data
    }

    //  Retorna todas las imagenes de una receta
    fun getAllMyImages (recipeid: String?): ArrayList<ImagesModel> {
        val data: ArrayList<ImagesModel> = ArrayList()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $tableImages WHERE $imageRecipeId = '$recipeid'"
        val cursor = db.rawQuery(selectALLQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    val images = ImagesModel(

                        cursor.getString(cursor.getColumnIndex(imageId)),
                        cursor.getString(cursor.getColumnIndex(imageRecipeId)),
                        cursor.getString(cursor.getColumnIndex(imagePath)),
                        cursor.getString(cursor.getColumnIndex(imageName)),
                        cursor.getString(cursor.getColumnIndex(imageDate))

                    )

                    data.add(images)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return data
    }

    //  Comprueba si el usuario tiene una receta en favoritos
    fun getFavoriteRecipe(userId: String, recipeid: String?): Int{
        var favorite = 0
        val db = readableDatabase
        val selectQuery = "SELECT $favoriteBoolean FROM $tableFavorites WHERE $favoriteUserId = '$userId' AND $favoriteRecipeId = '$recipeid'"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getInt(cursor.getColumnIndex(favoriteBoolean))
                    favorite = data

                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return favorite
    }

    //  Retorna el voto positivo de una receta ya puntuada
    fun getPositiveVote(recipeid: String, userid: String): Int {
        var vote = 0
        val db = readableDatabase
        val selectQuery = "SELECT $votePositive FROM $tableUsersVotes WHERE $voteRecipeId = '$recipeid' AND $voteUserId = '$userid'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getInt(cursor.getColumnIndex(votePositive))
                    vote = data
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return vote
    }

    //  Retorna el voto negativo de una receta ya puntuada
    fun getNegativeVote(recipeid: String, userid: String): Int {
        var vote = 0
        val db = readableDatabase
        val selectQuery = "SELECT $voteNegative FROM $tableUsersVotes WHERE $voteRecipeId = '$recipeid' AND $voteUserId = '$userid'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getInt(cursor.getColumnIndex(voteNegative))
                    vote = data
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return vote
    }

    //  Retorna el id del voto de una receta ya puntuada
    fun getIdVote(recipeid: String, userid: String): String {
        var vote = ""
        val db = readableDatabase
        val selectQuery = "SELECT $voteId FROM $tableUsersVotes WHERE $voteRecipeId = '$recipeid' AND $voteUserId = '$userid'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val data = cursor.getString(cursor.getColumnIndex(voteId))
                    vote = data
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return vote
    }

    //  Elimina una receta
    fun removeRecipe(id: String){
        val db = writableDatabase
        db.delete(tableRecipes, "$recipeId = '$id'", null)
        db.close()
    }

    //  Elimina los ingredientes de una receta
    fun removeIngredient(id: String){
        val db = writableDatabase
        db.delete(tableIngredients, "$ingredientId = '$id'", null)
        db.close()
    }

    //  Elimina las imágenes de una receta
    fun removeImage(id: String){
        val db = writableDatabase
        db.delete(tableImages, "$imageId = '$id'", null)
        db.close()
    }

    // Actualiza la ruta hacia la imágen de perfil del usuario
    fun updateImageUserPath(id: String, imagePath: String){
        val db = writableDatabase
        val values = ContentValues()

        values.put(usersLoginImage, imagePath)

        db.update(tableUsersLogin, values, "$usersLoginId = '$id'", null)
        db.close()
    }

    // Actualiza el nombre de la imágen de perfil del usuario
    fun updateImageUserName(id: String, imageName: String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put(usersLoginImageName, imageName)

        db.update(tableUsersLogin, values, "$usersLoginId = '$id'", null)
        db.close()
    }

    // Devuelve el nombre de la imágen de perfil del usuario
    fun getImageUserName(id: String): String {
        var imageUserName = ""
        val db = readableDatabase
        val selectQuery = "SELECT $usersLoginImageName FROM $tableUsersLogin WHERE $usersLoginId = '$id'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        val data = cursor.getString(cursor.getColumnIndex(usersLoginImageName))
                        imageUserName = data
                    } catch (e: IllegalStateException){}
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return imageUserName
    }

    // Devuelve la ruta hacia la imágen de perfil del usuario
    fun getImageUserProfilePath(id: String):String {
        var imagePath = ""
        val db = readableDatabase
        val selectQuery = "SELECT $usersLoginImage FROM $tableUsersLogin WHERE $usersLoginId = '$id'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        val data = cursor.getString(cursor.getColumnIndex(usersLoginImage))
                        imagePath = data
                    } catch (e: IllegalStateException){}
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        return imagePath
    }

    // Actualiza el nombre de perfil del usuario
    fun updateUserLoginName(id: String, userName: String){
        val db = writableDatabase
        val values = ContentValues()

        values.put(usersLoginName, userName)

        db.update(tableUsersLogin, values, "$usersLoginId = '$id'", null)
        db.close()
    }

    //  Actualiza el tipo de receta (Privada = 0 / Pública = 1)
    fun updateUserRecipeType(id: String, type:Int, date: String){
        val db = writableDatabase
        val values = ContentValues()

        values.put(recipeType, type)
        values.put(recipeDate, date)

        db.update(tableRecipes, values, "$recipeId = '$id'", null)
        db.close()
    }

    //  Actualiza el tipo de receta (Eliminar = 1)
    fun updateUserRecipeRemove(id: String, remove:Int, date: String){
        val db = writableDatabase
        val values = ContentValues()

        values.put(recipeRemove, remove)
        values.put(recipeDate, date)

        db.update(tableRecipes, values, "$recipeId = '$id'", null)
        db.close()
    }

    //  Actualiza las recetas guardadas en la tabla favoritas, marcada como favorita(1), desmarcada como tal(-1)
    fun updateFavoriteRecipe(userId: String, recipeid: String?, favorite: Int, date: String){
        val db = writableDatabase
        val values = ContentValues()

        values.put(favoriteBoolean, favorite)
        values.put(favoriteDate, date)

        db.update(tableFavorites, values, "$favoriteUserId = '$userId' AND $favoriteRecipeId = '$recipeid'", null)
        db.close()
    }

    //  Actualiza el voto positivo del usuario
    fun updateUserVotePositive(idRecipe: String, idUser: String, vote:Int){
        val db = writableDatabase
        val values = ContentValues()

        values.put(votePositive, vote)

        db.update(tableUsersVotes, values, "$voteRecipeId = '$idRecipe' AND $voteUserId = '$idUser'", null)
        db.close()
    }

    //  Actualiza el voto negativo del usuario
    fun updateUserVoteNegative(idRecipe: String, idUser: String, vote:Int){
        val db = writableDatabase
        val values = ContentValues()

        values.put(voteNegative, vote)

        db.update(tableUsersVotes, values, "$voteRecipeId = '$idRecipe' AND $voteUserId = '$idUser'", null)
        db.close()
    }

    //  Actualiza el voto positivo de la receta sumando uno
    fun updateRecipePositiveVoteAdd(idRecipe: String, vote:Int){
        val db = writableDatabase
        val values = ContentValues()

        values.put(recipePositiveVote, vote)

        db.update(tableRecipes, values, "$recipeId = '$idRecipe'", null)
        db.close()
    }

    //  Actualiza el voto positivo de la receta restando uno
    fun updateRecipePositiveVoteRemove(idRecipe: String, vote:Int){
        val db = writableDatabase
        val values = ContentValues()

        values.put(recipePositiveVote, vote)

        db.update(tableRecipes, values, "$recipeId = '$idRecipe'", null)
        db.close()
    }


    //  Actualiza el voto negativo de la receta sumando uno
    fun updateRecipeNegativeVoteAdd(idRecipe: String, vote:Int){
        val db = writableDatabase
        val values = ContentValues()

        values.put(recipeNegativeVote, vote)

        db.update(tableRecipes, values, "$recipeId = '$idRecipe'", null)
        db.close()
    }

    //  Actualiza el voto negativo de la receta restando uno
    fun updateRecipeNegativeVoteRemove(idRecipe: String, vote:Int){
        val db = writableDatabase
        val values = ContentValues()

        values.put(recipeNegativeVote, vote)

        db.update(tableRecipes, values, "$recipeId = '$idRecipe'", null)
        db.close()
    }


}