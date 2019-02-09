package info.papdt.express.helper.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.*
import info.papdt.express.helper.model.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = REPLACE)
    suspend fun add(category: Category)

    @Insert(onConflict = REPLACE)
    suspend fun addAll(categories: List<Category>)

    @Update(onConflict = REPLACE)
    suspend fun update(category: Category)

    @Query("SELECT * FROM category")
    suspend fun getAll(): List<Category>

    @Query("SELECT * FROM category WHERE title = :title LIMIT 1")
    suspend fun get(title: String): Category?

    @Delete
    suspend fun delete(category: Category)

    @Delete
    suspend fun delete(categories: List<Category>)

}