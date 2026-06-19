package com.digibrood.crmconnector.di;

import com.digibrood.crmconnector.data.local.AppDatabase;
import com.digibrood.crmconnector.data.local.dao.RecordingDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DatabaseModule_ProvideRecordingDaoFactory implements Factory<RecordingDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideRecordingDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public RecordingDao get() {
    return provideRecordingDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideRecordingDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideRecordingDaoFactory(dbProvider);
  }

  public static RecordingDao provideRecordingDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideRecordingDao(db));
  }
}
