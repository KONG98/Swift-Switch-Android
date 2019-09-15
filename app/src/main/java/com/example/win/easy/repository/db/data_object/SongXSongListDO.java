package com.example.win.easy.repository.db.data_object;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于表示Song与SongList多对多关系的表
 */
@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = SongDO.class,
                        parentColumns = "id",
                        childColumns = "songId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = SongListDO.class,
                        parentColumns = "id",
                        childColumns = "songListId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        primaryKeys = {
                "songId",
                "songListId"
        },
        indices = {
                @Index("songId"),
                @Index("songListId")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongXSongListDO {

    /**
     * ManyToMany关系的Song的id
     */
    @NonNull
    public Long songId;

    /**
     * ManyToMany关系的SongList的id
     */
    @NonNull
    public Long songListId;

}
