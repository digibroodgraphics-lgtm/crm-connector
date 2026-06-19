package com.digibrood.crmconnector.di;

import com.digibrood.crmconnector.data.local.AppDatabase;
import com.digibrood.crmconnector.data.local.dao.RemarkDao;
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
public final class DatabaseModule_ProvideRemarkDaoFactory implements Factory<RemarkDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideRemarkDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public RemarkDao get() {
    return provideRemarkDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideRemarkDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideRemarkDaoFactory(dbProvider);
  }

  public static RemarkDao provideRemarkDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideRemarkDao(db));
  }
}
