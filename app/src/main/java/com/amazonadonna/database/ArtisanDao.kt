import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Delete
import com.amazonadonna.model.Artisan

@Dao
interface ArtisanDao {
    @Query("SELECT * FROM artisan")
    fun getAll(): List<Artisan>

    @Query("SELECT * FROM artisan WHERE artisanID IN (:artisanIDs)")
    fun loadAllByIds(artisanIDs: IntArray): List<Artisan>

    @Query("SELECT * FROM artisan WHERE name LIKE :name " +
            "LIMIT 1")
    fun findByName(name: String): Artisan

    @Insert
    fun insertAll(vararg artisans: Artisan)

    @Delete
    fun delete(artisan: Artisan)
}