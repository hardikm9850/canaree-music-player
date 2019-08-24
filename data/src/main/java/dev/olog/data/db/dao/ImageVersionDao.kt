package dev.olog.data.db.dao

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.olog.data.db.entities.ImageVersionEntity

@Dao
@Keep
abstract class ImageVersionDao {

    @Query("SELECT * from image_version")
    abstract fun getAll(): List<ImageVersionEntity>

    @Query(
        """
        SELECT * 
        FROM image_version
        WHERE mediaId = :mediaId
    """
    )
    abstract fun getVersion(mediaId: String): ImageVersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertVersion(version: ImageVersionEntity)

    @Query("""
        DELETE FROM image_version
    """)
    abstract fun deleteAll()

}