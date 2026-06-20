package com.digibrood.crmconnector.receiver;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.repository.CallRepository;
import com.digibrood.crmconnector.util.PermissionManager;
import com.digibrood.crmconnector.worker.SyncScheduler;
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
public final class CallReceiver_MembersInjector implements MembersInjector<CallReceiver> {
  private final Provider<SyncScheduler> schedulerProvider;

  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<PermissionManager> permissionManagerProvider;

  private final Provider<CallRepository> callRepositoryProvider;

  public CallReceiver_MembersInjector(Provider<SyncScheduler> schedulerProvider,
      Provider<SecurePrefs> prefsProvider, Provider<PermissionManager> permissionManagerProvider,
      Provider<CallRepository> callRepositoryProvider) {
    this.schedulerProvider = schedulerProvider;
    this.prefsProvider = prefsProvider;
    this.permissionManagerProvider = permissionManagerProvider;
    this.callRepositoryProvider = callRepositoryProvider;
  }

  public static MembersInjector<CallReceiver> create(Provider<SyncScheduler> schedulerProvider,
      Provider<SecurePrefs> prefsProvider, Provider<PermissionManager> permissionManagerProvider,
      Provider<CallRepository> callRepositoryProvider) {
    return new CallReceiver_MembersInjector(schedulerProvider, prefsProvider, permissionManagerProvider, callRepositoryProvider);
  }

  @Override
  public void injectMembers(CallReceiver instance) {
    injectScheduler(instance, schedulerProvider.get());
    injectPrefs(instance, prefsProvider.get());
    injectPermissionManager(instance, permissionManagerProvider.get());
    injectCallRepository(instance, callRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.receiver.CallReceiver.scheduler")
  public static void injectScheduler(CallReceiver instance, SyncScheduler scheduler) {
    instance.scheduler = scheduler;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.receiver.CallReceiver.prefs")
  public static void injectPrefs(CallReceiver instance, SecurePrefs prefs) {
    instance.prefs = prefs;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.receiver.CallReceiver.permissionManager")
  public static void injectPermissionManager(CallReceiver instance,
      PermissionManager permissionManager) {
    instance.permissionManager = permissionManager;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.receiver.CallReceiver.callRepository")
  public static void injectCallRepository(CallReceiver instance, CallRepository callRepository) {
    instance.callRepository = callRepository;
  }
}
