package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PrayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PrayerEntity)

    @Query("SELECT * FROM prayer_cache WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): PrayerEntity?

    @Query("SELECT * FROM prayer_cache WHERE key = :key LIMIT 1")
    fun observeByKey(key: String): Flow<PrayerEntity?>

    @Query("SELECT * FROM prayer_cache WHERE locationId = :locationId AND dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC")
    suspend fun getRange(locationId: Int, startDate: String, endDate: String): List<PrayerEntity>

    @Query("SELECT COUNT(*) FROM prayer_cache WHERE locationId = :locationId AND dateString >= :fromDate")
    suspend fun countFutureCachedDays(locationId: Int, fromDate: String): Int

    @Query("DELETE FROM prayer_cache WHERE dateString < :beforeDate")
    suspend fun deleteOldCache(beforeDate: String)
}
