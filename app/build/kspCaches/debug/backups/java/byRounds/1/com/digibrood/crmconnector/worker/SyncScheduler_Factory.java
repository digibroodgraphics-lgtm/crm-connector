package com.digibrood.crmconnector.worker;

import androidx.work.WorkManager;
import com.digibrood.crmconnector.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SyncScheduler_Factory implements Factory<SyncScheduler> {
  private final Provider<WorkManager> workManagerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public SyncScheduler_Factory(Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.workManagerProvider = workManagerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SyncScheduler get() {
    return newInstance(workManagerProvider.get(), settingsRepositoryProvider.get());
  }

  public static SyncScheduler_Factory create(Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SyncScheduler_Factory(workManagerProvider, settingsRepositoryProvider);
  }

  public static SyncScheduler newInstance(WorkManager workManager,
      SettingsRepository settingsRepository) {
    return new SyncScheduler(workManager, settingsRepository);
  }
}
