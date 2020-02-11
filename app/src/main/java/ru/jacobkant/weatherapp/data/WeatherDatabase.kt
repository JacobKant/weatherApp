package ru.jacobkant.weatherapp.data

import androidx.room.*
import io.reactivex.Single

@Entity(tableName = "city")
data class CityRow(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "name") val name: String
) {
    override fun toString(): String {
        return name
    }
}

@Dao
interface CityDao {
    @Query("SELECT id, name FROM city WHERE name LIKE :namePart LIMIT 100")
    fun findByName(namePart: String): Single<List<CityRow>>
}

@Database(entities = [CityRow::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
}