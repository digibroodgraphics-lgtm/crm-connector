package com.digibrood.crmconnector.receiver;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.sync.SyncManager;
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
public final class BootReceiver_MembersInjector implements MembersInjector<BootReceiver> {
  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<SyncManager> syncManagerProvider;

  public BootReceiver_MembersInjector(Provider<SecurePrefs> prefsProvider,
      Provider<SyncManager> syncManagerProvider) {
    this.prefsProvider = prefsProvider;
    this.syncManagerProvider = syncManagerProvider;
  }

  public static MembersInjector<BootReceiver> create(Provider<SecurePrefs> prefsProvider,
      Provider<SyncManager> syncManagerProvider) {
    return new BootReceiver_MembersInjector(prefsProvider, syncManagerProvider);
  }

  @Override
  public void injectMembers(BootReceiver instance) {
    injectPrefs(instance, prefsProvider.get());
    injectSyncManager(instance, syncManagerProvider.get());
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.receiver.BootReceiver.prefs")
  public static void injectPrefs(BootReceiver instance, SecurePrefs prefs) {
    instance.prefs = prefs;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.receiver.BootReceiver.syncManager")
  public static void injectSyncManager(BootReceiver instance, SyncManager syncManager) {
    instance.syncManager = syncManager;
  }
}
