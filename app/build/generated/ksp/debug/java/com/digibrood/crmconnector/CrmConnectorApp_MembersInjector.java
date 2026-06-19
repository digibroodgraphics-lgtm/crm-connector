package com.digibrood.crmconnector;

import androidx.hilt.work.HiltWorkerFactory;
import com.digibrood.crmconnector.util.CrashReporter;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class CrmConnectorApp_MembersInjector implements MembersInjector<CrmConnectorApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private final Provider<CrashReporter> crashReporterProvider;

  public CrmConnectorApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<CrashReporter> crashReporterProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
    this.crashReporterProvider = crashReporterProvider;
  }

  public static MembersInjector<CrmConnectorApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<CrashReporter> crashReporterProvider) {
    return new CrmConnectorApp_MembersInjector(workerFactoryProvider, crashReporterProvider);
  }

  @Override
  public void injectMembers(CrmConnectorApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
    injectCrashReporter(instance, crashReporterProvider.get());
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.CrmConnectorApp.workerFactory")
  public static void injectWorkerFactory(CrmConnectorApp instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.CrmConnectorApp.crashReporter")
  public static void injectCrashReporter(CrmConnectorApp instance, CrashReporter crashReporter) {
    instance.crashReporter = crashReporter;
  }
}
