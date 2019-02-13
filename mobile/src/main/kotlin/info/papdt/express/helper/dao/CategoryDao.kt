package info.papdt.express.helper.dao

import android.content.Context
import androidx.room.*
import androidx.room.OnConflictStrategy.*
import info.papdt.express.helper.R
import info.papdt.express.helper.model.Category

@Dao
abstract class CategoryDao {

    companion object {

        private val DEFAULT_CATEGORIES_PAIRS = mapOf(
                R.string.default_category_art to "insert_photo",
                R.string.default_category_books to "book",
                R.string.default_category_daily_necessities to "home",
                R.string.default_category_digital_product to "phonelink",
                R.string.default_category_entertainment to "games",
                R.string.default_category_food_snack to "restaurant_menu",
                R.string.default_category_home_appliance to "power",
                R.string.default_category_letters to "email"
        )

    }

    suspend fun add(title: String, iconCode: String): Category {
        return Category(title, iconCode).also { add(it) }
    }

    @Insert(onConflict = REPLACE)
    abstract suspend fun add(category: Category)

    @Insert(onConflict = REPLACE)
    abstract suspend fun addAll(categories: List<Category>)

    @Update(onConflict = REPLACE)
    abstract suspend fun update(category: Category)

    @Query("SELECT * FROM category")
    abstract suspend fun getAll(): List<Category>

    @Query("SELECT * FROM category WHERE title = :title LIMIT 1")
    abstract suspend fun get(title: String): Category?

    @Delete
    abstract suspend fun delete(category: Category)

    @Delete
    abstract suspend fun delete(categories: List<Category>)

    suspend fun clear() {
        delete(getAll())
    }

    suspend fun deleteWithUpdatingPackages(context: Context, category: Category) {
        val packDatabase = PackageDatabase.getInstance(context)
        for (item in packDatabase.data) {
            if (item.categoryTitle == category.title) {
                item.categoryTitle = null
            }
        }
        packDatabase.save()
        delete(category)
    }

    suspend fun clearWithUpdatingPackages(context: Context) {
        val packDatabase = PackageDatabase.getInstance(context)
        for (item in packDatabase.data) {
            item.categoryTitle = null
        }
        packDatabase.save()
        clear()
    }

    suspend fun addDefaultCategories(context: Context) {
        for ((titleRes, iconCode) in DEFAULT_CATEGORIES_PAIRS) {
            add(context.getString(titleRes), iconCode)
        }
    }

}