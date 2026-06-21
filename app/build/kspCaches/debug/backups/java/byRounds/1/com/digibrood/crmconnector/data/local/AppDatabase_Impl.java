package com.digibrood.crmconnector.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.digibrood.crmconnector.data.local.dao.CallDao;
import com.digibrood.crmconnector.data.local.dao.CallDao_Impl;
import com.digibrood.crmconnector.data.local.dao.RecordingDao;
import com.digibrood.crmconnector.data.local.dao.RecordingDao_Impl;
import com.digibrood.crmconnector.data.local.dao.RemarkDao;
import com.digibrood.crmconnector.data.local.dao.RemarkDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CallDao _callDao;

  private volatile RecordingDao _recordingDao;

  private volatile RemarkDao _remarkDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `calls` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientCallId` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `callType` TEXT NOT NULL, `hasRecording` INTEGER NOT NULL, `syncState` TEXT NOT NULL, `attemptCount` INTEGER NOT NULL, `lastAttemptAt` INTEGER NOT NULL, `serverCallId` TEXT, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_calls_clientCallId` ON `calls` (`clientCallId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calls_syncState` ON `calls` (`syncState`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calls_startTime` ON `calls` (`startTime`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `recordings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientCallId` TEXT NOT NULL, `filePath` TEXT NOT NULL, `fileName` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `recordedAt` INTEGER NOT NULL, `uploadState` TEXT NOT NULL, `attemptCount` INTEGER NOT NULL, `lastAttemptAt` INTEGER NOT NULL, `recordingId` TEXT, `objectKey` TEXT, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_recordings_filePath` ON `recordings` (`filePath`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recordings_uploadState` ON `recordings` (`uploadState`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recordings_clientCallId` ON `recordings` (`clientCallId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `remarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientCallId` TEXT, `phoneNumber` TEXT NOT NULL, `contactName` TEXT, `company` TEXT, `callType` TEXT, `remark` TEXT NOT NULL, `status` TEXT, `syncState` TEXT NOT NULL, `attemptCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_remarks_syncState` ON `remarks` (`syncState`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '77b08502b1e1373a025f38b8bce5e011')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `calls`");
        db.execSQL("DROP TABLE IF EXISTS `recordings`");
        db.execSQL("DROP TABLE IF EXISTS `remarks`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCalls = new HashMap<String, TableInfo.Column>(13);
        _columnsCalls.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("clientCallId", new TableInfo.Column("clientCallId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("duration", new TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("callType", new TableInfo.Column("callType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("hasRecording", new TableInfo.Column("hasRecording", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("syncState", new TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("attemptCount", new TableInfo.Column("attemptCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("lastAttemptAt", new TableInfo.Column("lastAttemptAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("serverCallId", new TableInfo.Column("serverCallId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalls.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCalls = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCalls = new HashSet<TableInfo.Index>(3);
        _indicesCalls.add(new TableInfo.Index("index_calls_clientCallId", true, Arrays.asList("clientCallId"), Arrays.asList("ASC")));
        _indicesCalls.add(new TableInfo.Index("index_calls_syncState", false, Arrays.asList("syncState"), Arrays.asList("ASC")));
        _indicesCalls.add(new TableInfo.Index("index_calls_startTime", false, Arrays.asList("startTime"), Arrays.asList("ASC")));
        final TableInfo _infoCalls = new TableInfo("calls", _columnsCalls, _foreignKeysCalls, _indicesCalls);
        final TableInfo _existingCalls = TableInfo.read(db, "calls");
        if (!_infoCalls.equals(_existingCalls)) {
          return new RoomOpenHelper.ValidationResult(false, "calls(com.digibrood.crmconnector.data.local.entity.CallEntity).\n"
                  + " Expected:\n" + _infoCalls + "\n"
                  + " Found:\n" + _existingCalls);
        }
        final HashMap<String, TableInfo.Column> _columnsRecordings = new HashMap<String, TableInfo.Column>(13);
        _columnsRecordings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("clientCallId", new TableInfo.Column("clientCallId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("filePath", new TableInfo.Column("filePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("mimeType", new TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("fileSize", new TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("recordedAt", new TableInfo.Column("recordedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("uploadState", new TableInfo.Column("uploadState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("attemptCount", new TableInfo.Column("attemptCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("lastAttemptAt", new TableInfo.Column("lastAttemptAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("recordingId", new TableInfo.Column("recordingId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("objectKey", new TableInfo.Column("objectKey", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecordings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecordings = new HashSet<TableInfo.Index>(3);
        _indicesRecordings.add(new TableInfo.Index("index_recordings_filePath", true, Arrays.asList("filePath"), Arrays.asList("ASC")));
        _indicesRecordings.add(new TableInfo.Index("index_recordings_uploadState", false, Arrays.asList("uploadState"), Arrays.asList("ASC")));
        _indicesRecordings.add(new TableInfo.Index("index_recordings_clientCallId", false, Arrays.asList("clientCallId"), Arrays.asList("ASC")));
        final TableInfo _infoRecordings = new TableInfo("recordings", _columnsRecordings, _foreignKeysRecordings, _indicesRecordings);
        final TableInfo _existingRecordings = TableInfo.read(db, "recordings");
        if (!_infoRecordings.equals(_existingRecordings)) {
          return new RoomOpenHelper.ValidationResult(false, "recordings(com.digibrood.crmconnector.data.local.entity.RecordingEntity).\n"
                  + " Expected:\n" + _infoRecordings + "\n"
                  + " Found:\n" + _existingRecordings);
        }
        final HashMap<String, TableInfo.Column> _columnsRemarks = new HashMap<String, TableInfo.Column>(11);
        _columnsRemarks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("clientCallId", new TableInfo.Column("clientCallId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("contactName", new TableInfo.Column("contactName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("company", new TableInfo.Column("company", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("callType", new TableInfo.Column("callType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("remark", new TableInfo.Column("remark", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("syncState", new TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("attemptCount", new TableInfo.Column("attemptCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRemarks.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRemarks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRemarks = new HashSet<TableInfo.Index>(1);
        _indicesRemarks.add(new TableInfo.Index("index_remarks_syncState", false, Arrays.asList("syncState"), Arrays.asList("ASC")));
        final TableInfo _infoRemarks = new TableInfo("remarks", _columnsRemarks, _foreignKeysRemarks, _indicesRemarks);
        final TableInfo _existingRemarks = TableInfo.read(db, "remarks");
        if (!_infoRemarks.equals(_existingRemarks)) {
          return new RoomOpenHelper.ValidationResult(false, "remarks(com.digibrood.crmconnector.data.local.entity.RemarkEntity).\n"
                  + " Expected:\n" + _infoRemarks + "\n"
                  + " Found:\n" + _existingRemarks);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "77b08502b1e1373a025f38b8bce5e011", "7573eee2fd420c853b3eb55055c5f279");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "calls","recordings","remarks");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `calls`");
      _db.execSQL("DELETE FROM `recordings`");
      _db.execSQL("DELETE FROM `remarks`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CallDao.class, CallDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RecordingDao.class, RecordingDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RemarkDao.class, RemarkDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CallDao callDao() {
    if (_callDao != null) {
      return _callDao;
    } else {
      synchronized(this) {
        if(_callDao == null) {
          _callDao = new CallDao_Impl(this);
        }
        return _callDao;
      }
    }
  }

  @Override
  public RecordingDao recordingDao() {
    if (_recordingDao != null) {
      return _recordingDao;
    } else {
      synchronized(this) {
        if(_recordingDao == null) {
          _recordingDao = new RecordingDao_Impl(this);
        }
        return _recordingDao;
      }
    }
  }

  @Override
  public RemarkDao remarkDao() {
    if (_remarkDao != null) {
      return _remarkDao;
    } else {
      synchronized(this) {
        if(_remarkDao == null) {
          _remarkDao = new RemarkDao_Impl(this);
        }
        return _remarkDao;
      }
    }
  }
}
