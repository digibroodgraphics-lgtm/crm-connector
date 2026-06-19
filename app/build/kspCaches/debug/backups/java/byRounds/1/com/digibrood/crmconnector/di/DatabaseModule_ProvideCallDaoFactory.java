package com.digibrood.crmconnector.di;

import com.digibrood.crmconnector.data.local.AppDatabase;
import com.digibrood.crmconnector.data.local.dao.CallDao;
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
public final class DatabaseModule_ProvideCallDaoFactory implements Factory<CallDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideCallDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CallDao get() {
    return provideCallDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideCallDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideCallDaoFactory(dbProvider);
  }

  public static CallDao provideCallDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCallDao(db));
  }
}
