package com.example.win.easy.repository.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.win.easy.repository.db.data_object.SongDO;

import java.util.Collection;
import java.util.List;

@Dao
public interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SongDO songDO);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(Collection<SongDO> songDOs);

    @Query("SELECT * FROM SongDO")
    LiveData<List<SongDO>> findAllSongDOs();

    @Query("SELECT * FROM SongDO")
    List<SongDO> findAllDataSongDOs();

    @Query("SELECT * FROM SongDO WHERE id=:songId")
//    SongDO findById(long songId);
    LiveData<SongDO> findById(long songId);

    @Query("SELECT * FROM SongDO WHERE id=:songId")
    SongDO findDataById(long songId);


    @Query("SELECT * FROM SongDO WHERE source=:dataSource")
//    List<SongDO> findByDataSource(String dataSource);
    LiveData<List<SongDO>> findByDataSource(String dataSource);

    @Query("SELECT * FROM SongDO WHERE source!=:dataSource")
    List<SongDO> findAllSongsExceptByDataSource(String dataSource);

    @Query("SELECT * FROM SongDO WHERE name=:name AND author=:author AND source=:source AND uid=:uid AND remoteId=:remoteId")
    SongDO findLocalRecordOfNetworkSong(String name, String author, String source, String uid, String remoteId);

    @Query("SELECT * FROM SongDO WHERE songPath=:songPath")
    SongDO findAllBySongPath(String songPath);

    @Delete
    void delete(SongDO songDO);

    @Delete
    void delete(Collection<SongDO> songDOs);

    @Update
    void update(SongDO songDO);

    @Update
    void update(Collection<SongDO> songDOs);
}
