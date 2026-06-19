package com.digibrood.crmconnector.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.digibrood.crmconnector.data.local.entity.RemarkEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RemarkDao_Impl implements RemarkDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RemarkEntity> __insertionAdapterOfRemarkEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkState;

  private final SharedSQLiteStatement __preparedStmtOfPurgeSyncedOlderThan;

  public RemarkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRemarkEntity = new EntityInsertionAdapter<RemarkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `remarks` (`id`,`clientCallId`,`phoneNumber`,`contactName`,`company`,`remark`,`status`,`syncState`,`attemptCount`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RemarkEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getClientCallId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getClientCallId());
        }
        statement.bindString(3, entity.getPhoneNumber());
        if (entity.getContactName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getContactName());
        }
        if (entity.getCompany() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCompany());
        }
        statement.bindString(6, entity.getRemark());
        if (entity.getStatus() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getStatus());
        }
        statement.bindString(8, entity.getSyncState());
        statement.bindLong(9, entity.getAttemptCount());
        statement.bindLong(10, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfMarkState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE remarks SET syncState = ?, attemptCount = attemptCount + 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfPurgeSyncedOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM remarks WHERE syncState = 'SYNCED' AND createdAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final RemarkEntity remark, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRemarkEntity.insertAndReturnId(remark);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markState(final long id, final String state,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkState.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, state);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object purgeSyncedOlderThan(final long olderThan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeSyncedOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, olderThan);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfPurgeSyncedOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getByState(final String state, final int limit,
      final Continuation<? super List<RemarkEntity>> $completion) {
    final String _sql = "SELECT * FROM remarks WHERE syncState = ? ORDER BY createdAt ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, state);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RemarkEntity>>() {
      @Override
      @NonNull
      public List<RemarkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientCallId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientCallId");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfContactName = CursorUtil.getColumnIndexOrThrow(_cursor, "contactName");
          final int _cursorIndexOfCompany = CursorUtil.getColumnIndexOrThrow(_cursor, "company");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final int _cursorIndexOfAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attemptCount");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<RemarkEntity> _result = new ArrayList<RemarkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RemarkEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpClientCallId;
            if (_cursor.isNull(_cursorIndexOfClientCallId)) {
              _tmpClientCallId = null;
            } else {
              _tmpClientCallId = _cursor.getString(_cursorIndexOfClientCallId);
            }
            final String _tmpPhoneNumber;
            _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            final String _tmpContactName;
            if (_cursor.isNull(_cursorIndexOfContactName)) {
              _tmpContactName = null;
            } else {
              _tmpContactName = _cursor.getString(_cursorIndexOfContactName);
            }
            final String _tmpCompany;
            if (_cursor.isNull(_cursorIndexOfCompany)) {
              _tmpCompany = null;
            } else {
              _tmpCompany = _cursor.getString(_cursorIndexOfCompany);
            }
            final String _tmpRemark;
            _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSyncState;
            _tmpSyncState = _cursor.getString(_cursorIndexOfSyncState);
            final int _tmpAttemptCount;
            _tmpAttemptCount = _cursor.getInt(_cursorIndexOfAttemptCount);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new RemarkEntity(_tmpId,_tmpClientCallId,_tmpPhoneNumber,_tmpContactName,_tmpCompany,_tmpRemark,_tmpStatus,_tmpSyncState,_tmpAttemptCount,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
