package com.digibrood.crmconnector.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.digibrood.crmconnector.data.local.entity.RecordingEntity;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RecordingDao_Impl implements RecordingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecordingEntity> __insertionAdapterOfRecordingEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkState;

  private final SharedSQLiteStatement __preparedStmtOfMarkUploaded;

  private final SharedSQLiteStatement __preparedStmtOfPurgeUploadedOlderThan;

  public RecordingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecordingEntity = new EntityInsertionAdapter<RecordingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `recordings` (`id`,`clientCallId`,`phoneNumber`,`filePath`,`fileName`,`mimeType`,`fileSize`,`recordedAt`,`uploadState`,`attemptCount`,`lastAttemptAt`,`recordingId`,`objectKey`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordingEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getClientCallId());
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPhoneNumber());
        }
        statement.bindString(4, entity.getFilePath());
        statement.bindString(5, entity.getFileName());
        statement.bindString(6, entity.getMimeType());
        statement.bindLong(7, entity.getFileSize());
        statement.bindLong(8, entity.getRecordedAt());
        statement.bindString(9, entity.getUploadState());
        statement.bindLong(10, entity.getAttemptCount());
        statement.bindLong(11, entity.getLastAttemptAt());
        if (entity.getRecordingId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getRecordingId());
        }
        if (entity.getObjectKey() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getObjectKey());
        }
        statement.bindLong(14, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfMarkState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recordings SET uploadState = ?, attemptCount = attemptCount + 1, lastAttemptAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkUploaded = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recordings SET uploadState = 'UPLOADED', recordingId = ?, objectKey = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfPurgeUploadedOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recordings WHERE uploadState = 'UPLOADED' AND createdAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final RecordingEntity recording,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRecordingEntity.insertAndReturnId(recording);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markState(final long id, final String state, final long now,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkState.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, state);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, now);
        _argIndex = 3;
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
  public Object markUploaded(final long id, final String recordingId, final String objectKey,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkUploaded.acquire();
        int _argIndex = 1;
        if (recordingId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, recordingId);
        }
        _argIndex = 2;
        if (objectKey == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, objectKey);
        }
        _argIndex = 3;
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
          __preparedStmtOfMarkUploaded.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object purgeUploadedOlderThan(final long olderThan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeUploadedOlderThan.acquire();
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
          __preparedStmtOfPurgeUploadedOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object existsByPath(final String filePath,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM recordings WHERE filePath = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, filePath);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getByState(final String state, final int limit,
      final Continuation<? super List<RecordingEntity>> $completion) {
    final String _sql = "SELECT * FROM recordings WHERE uploadState = ? ORDER BY recordedAt ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, state);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordingEntity>>() {
      @Override
      @NonNull
      public List<RecordingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientCallId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientCallId");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfUploadState = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadState");
          final int _cursorIndexOfAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attemptCount");
          final int _cursorIndexOfLastAttemptAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAttemptAt");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfObjectKey = CursorUtil.getColumnIndexOrThrow(_cursor, "objectKey");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<RecordingEntity> _result = new ArrayList<RecordingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordingEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpClientCallId;
            _tmpClientCallId = _cursor.getString(_cursorIndexOfClientCallId);
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final String _tmpUploadState;
            _tmpUploadState = _cursor.getString(_cursorIndexOfUploadState);
            final int _tmpAttemptCount;
            _tmpAttemptCount = _cursor.getInt(_cursorIndexOfAttemptCount);
            final long _tmpLastAttemptAt;
            _tmpLastAttemptAt = _cursor.getLong(_cursorIndexOfLastAttemptAt);
            final String _tmpRecordingId;
            if (_cursor.isNull(_cursorIndexOfRecordingId)) {
              _tmpRecordingId = null;
            } else {
              _tmpRecordingId = _cursor.getString(_cursorIndexOfRecordingId);
            }
            final String _tmpObjectKey;
            if (_cursor.isNull(_cursorIndexOfObjectKey)) {
              _tmpObjectKey = null;
            } else {
              _tmpObjectKey = _cursor.getString(_cursorIndexOfObjectKey);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new RecordingEntity(_tmpId,_tmpClientCallId,_tmpPhoneNumber,_tmpFilePath,_tmpFileName,_tmpMimeType,_tmpFileSize,_tmpRecordedAt,_tmpUploadState,_tmpAttemptCount,_tmpLastAttemptAt,_tmpRecordingId,_tmpObjectKey,_tmpCreatedAt);
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

  @Override
  public Object getByCall(final String clientCallId,
      final Continuation<? super RecordingEntity> $completion) {
    final String _sql = "SELECT * FROM recordings WHERE clientCallId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, clientCallId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecordingEntity>() {
      @Override
      @Nullable
      public RecordingEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientCallId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientCallId");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final int _cursorIndexOfUploadState = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadState");
          final int _cursorIndexOfAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "attemptCount");
          final int _cursorIndexOfLastAttemptAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAttemptAt");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfObjectKey = CursorUtil.getColumnIndexOrThrow(_cursor, "objectKey");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final RecordingEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpClientCallId;
            _tmpClientCallId = _cursor.getString(_cursorIndexOfClientCallId);
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            final String _tmpUploadState;
            _tmpUploadState = _cursor.getString(_cursorIndexOfUploadState);
            final int _tmpAttemptCount;
            _tmpAttemptCount = _cursor.getInt(_cursorIndexOfAttemptCount);
            final long _tmpLastAttemptAt;
            _tmpLastAttemptAt = _cursor.getLong(_cursorIndexOfLastAttemptAt);
            final String _tmpRecordingId;
            if (_cursor.isNull(_cursorIndexOfRecordingId)) {
              _tmpRecordingId = null;
            } else {
              _tmpRecordingId = _cursor.getString(_cursorIndexOfRecordingId);
            }
            final String _tmpObjectKey;
            if (_cursor.isNull(_cursorIndexOfObjectKey)) {
              _tmpObjectKey = null;
            } else {
              _tmpObjectKey = _cursor.getString(_cursorIndexOfObjectKey);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new RecordingEntity(_tmpId,_tmpClientCallId,_tmpPhoneNumber,_tmpFilePath,_tmpFileName,_tmpMimeType,_tmpFileSize,_tmpRecordedAt,_tmpUploadState,_tmpAttemptCount,_tmpLastAttemptAt,_tmpRecordingId,_tmpObjectKey,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object uploadedSince(final long since, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM recordings WHERE uploadState = 'UPLOADED' AND lastAttemptAt >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> pendingCount() {
    final String _sql = "SELECT COUNT(*) FROM recordings WHERE uploadState IN ('PENDING', 'FAILED')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recordings"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
