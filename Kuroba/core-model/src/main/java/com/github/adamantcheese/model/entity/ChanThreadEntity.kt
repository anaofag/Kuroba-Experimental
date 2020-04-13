package com.github.adamantcheese.model.entity

import androidx.room.*

@Entity(
        tableName = ChanThreadEntity.TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = ChanBoardEntity::class,
                    parentColumns = [ChanBoardEntity.BOARD_ID_COLUMN_NAME],
                    childColumns = [ChanThreadEntity.OWNER_BOARD_ID_COLUMN_NAME],
                    onUpdate = ForeignKey.CASCADE,
                    onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index(
                    name = ChanThreadEntity.THREAD_ID_OWNER_BOARD_ID_INDEX_NAME,
                    value = [
                        ChanThreadEntity.THREAD_NO_COLUMN_NAME,
                        ChanThreadEntity.OWNER_BOARD_ID_COLUMN_NAME
                    ],
                    unique = true
            )
        ]
)
data class ChanThreadEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = THREAD_ID_COLUMN_NAME)
        var threadId: Long,
        @ColumnInfo(name = THREAD_NO_COLUMN_NAME)
        val threadNo: Long,
        @ColumnInfo(name = OWNER_BOARD_ID_COLUMN_NAME)
        val ownerBoardId: Long
) {

    companion object {
        const val TABLE_NAME = "chan_thread"

        const val THREAD_ID_COLUMN_NAME = "thread_id"
        const val THREAD_NO_COLUMN_NAME = "thread_no"
        const val OWNER_BOARD_ID_COLUMN_NAME = "owner_board_id"

        const val THREAD_ID_OWNER_BOARD_ID_INDEX_NAME = "${TABLE_NAME}_thread_no_owner_board_id_idx"
    }
}