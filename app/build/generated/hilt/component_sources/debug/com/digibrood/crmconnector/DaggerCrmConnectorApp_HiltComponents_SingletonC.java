package com.digibrood.crmconnector;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.digibrood.crmconnector.data.local.AppDatabase;
import com.digibrood.crmconnector.data.local.dao.CallDao;
import com.digibrood.crmconnector.data.local.dao.RecordingDao;
import com.digibrood.crmconnector.data.local.dao.RemarkDao;
import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.remote.api.CrmApiService;
import com.digibrood.crmconnector.data.remote.interceptor.AuthInterceptor;
import com.digibrood.crmconnector.data.remote.interceptor.DynamicBaseUrlInterceptor;
import com.digibrood.crmconnector.data.remote.interceptor.HttpsEnforcementInterceptor;
import com.digibrood.crmconnector.data.remote.interceptor.TokenAuthenticator;
import com.digibrood.crmconnector.data.repository.AuthRepository;
import com.digibrood.crmconnector.data.repository.BrandingRepository;
import com.digibrood.crmconnector.data.repository.CallRepository;
import com.digibrood.crmconnector.data.repository.ContactRepository;
import com.digibrood.crmconnector.data.repository.DeviceRepository;
import com.digibrood.crmconnector.data.repository.RecordingRepository;
import com.digibrood.crmconnector.data.repository.SettingsRepository;
import com.digibrood.crmconnector.data.repository.StatsRepository;
import com.digibrood.crmconnector.di.AppModule_ProvideWorkManagerFactory;
import com.digibrood.crmconnector.di.DatabaseModule_ProvideCallDaoFactory;
import com.digibrood.crmconnector.di.DatabaseModule_ProvideDatabaseFactory;
import com.digibrood.crmconnector.di.DatabaseModule_ProvideRecordingDaoFactory;
import com.digibrood.crmconnector.di.DatabaseModule_ProvideRemarkDaoFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideCrmApiServiceFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideLoggingInterceptorFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideMainOkHttpClientFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideMainRetrofitFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideMoshiFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideRefreshApiServiceFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideRefreshOkHttpClientFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideRefreshRetrofitFactory;
import com.digibrood.crmconnector.di.NetworkModule_ProvideUploadOkHttpClientFactory;
import com.digibrood.crmconnector.overlay.CallPopupActivity;
import com.digibrood.crmconnector.overlay.CallPopupViewModel;
import com.digibrood.crmconnector.overlay.CallPopupViewModel_HiltModules;
import com.digibrood.crmconnector.receiver.BootReceiver;
import com.digibrood.crmconnector.receiver.BootReceiver_MembersInjector;
import com.digibrood.crmconnector.receiver.CallReceiver;
import com.digibrood.crmconnector.receiver.CallReceiver_MembersInjector;
import com.digibrood.crmconnector.service.NotificationHelper;
import com.digibrood.crmconnector.service.SyncForegroundService;
import com.digibrood.crmconnector.service.SyncForegroundService_MembersInjector;
import com.digibrood.crmconnector.sync.SyncController;
import com.digibrood.crmconnector.sync.SyncManager;
import com.digibrood.crmconnector.ui.navigation.StartDestinationProvider;
import com.digibrood.crmconnector.ui.screens.dashboard.DashboardViewModel;
import com.digibrood.crmconnector.ui.screens.dashboard.DashboardViewModel_HiltModules;
import com.digibrood.crmconnector.ui.screens.login.LoginViewModel;
import com.digibrood.crmconnector.ui.screens.login.LoginViewModel_HiltModules;
import com.digibrood.crmconnector.ui.screens.permissions.PermissionsViewModel;
import com.digibrood.crmconnector.ui.screens.permissions.PermissionsViewModel_HiltModules;
import com.digibrood.crmconnector.ui.screens.register.RegisterViewModel;
import com.digibrood.crmconnector.ui.screens.register.RegisterViewModel_HiltModules;
import com.digibrood.crmconnector.ui.screens.session.SessionViewModel;
import com.digibrood.crmconnector.ui.screens.session.SessionViewModel_HiltModules;
import com.digibrood.crmconnector.ui.screens.splash.SplashViewModel;
import com.digibrood.crmconnector.ui.screens.splash.SplashViewModel_HiltModules;
import com.digibrood.crmconnector.util.CallLogReader;
import com.digibrood.crmconnector.util.ConnectivityObserver;
import com.digibrood.crmconnector.util.ContactReader;
import com.digibrood.crmconnector.util.CrashReporter;
import com.digibrood.crmconnector.util.DeviceInfoProvider;
import com.digibrood.crmconnector.util.PermissionManager;
import com.digibrood.crmconnector.util.RecordingScanner;
import com.digibrood.crmconnector.worker.HeartbeatWorker;
import com.digibrood.crmconnector.worker.HeartbeatWorker_AssistedFactory;
import com.digibrood.crmconnector.worker.RecordingUploadWorker;
import com.digibrood.crmconnector.worker.RecordingUploadWorker_AssistedFactory;
import com.digibrood.crmconnector.worker.SyncScheduler;
import com.digibrood.crmconnector.worker.SyncWorker;
import com.digibrood.crmconnector.worker.SyncWorker_AssistedFactory;
import com.squareup.moshi.Moshi;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

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
public final class DaggerCrmConnectorApp_HiltComponents_SingletonC {
  private DaggerCrmConnectorApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CrmConnectorApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CrmConnectorApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CrmConnectorApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CrmConnectorApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CrmConnectorApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CrmConnectorApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CrmConnectorApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CrmConnectorApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CrmConnectorApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CrmConnectorApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CrmConnectorApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CrmConnectorApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CrmConnectorApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CrmConnectorApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CrmConnectorApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CrmConnectorApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CrmConnectorApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CrmConnectorApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CrmConnectorApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public void injectCallPopupActivity(CallPopupActivity callPopupActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(7).put(LazyClassKeyProvider.com_digibrood_crmconnector_overlay_CallPopupViewModel, CallPopupViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_dashboard_DashboardViewModel, DashboardViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_login_LoginViewModel, LoginViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_permissions_PermissionsViewModel, PermissionsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_register_RegisterViewModel, RegisterViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_session_SessionViewModel, SessionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_splash_SplashViewModel, SplashViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_digibrood_crmconnector_ui_screens_dashboard_DashboardViewModel = "com.digibrood.crmconnector.ui.screens.dashboard.DashboardViewModel";

      static String com_digibrood_crmconnector_ui_screens_permissions_PermissionsViewModel = "com.digibrood.crmconnector.ui.screens.permissions.PermissionsViewModel";

      static String com_digibrood_crmconnector_overlay_CallPopupViewModel = "com.digibrood.crmconnector.overlay.CallPopupViewModel";

      static String com_digibrood_crmconnector_ui_screens_login_LoginViewModel = "com.digibrood.crmconnector.ui.screens.login.LoginViewModel";

      static String com_digibrood_crmconnector_ui_screens_session_SessionViewModel = "com.digibrood.crmconnector.ui.screens.session.SessionViewModel";

      static String com_digibrood_crmconnector_ui_screens_splash_SplashViewModel = "com.digibrood.crmconnector.ui.screens.splash.SplashViewModel";

      static String com_digibrood_crmconnector_ui_screens_register_RegisterViewModel = "com.digibrood.crmconnector.ui.screens.register.RegisterViewModel";

      @KeepFieldType
      DashboardViewModel com_digibrood_crmconnector_ui_screens_dashboard_DashboardViewModel2;

      @KeepFieldType
      PermissionsViewModel com_digibrood_crmconnector_ui_screens_permissions_PermissionsViewModel2;

      @KeepFieldType
      CallPopupViewModel com_digibrood_crmconnector_overlay_CallPopupViewModel2;

      @KeepFieldType
      LoginViewModel com_digibrood_crmconnector_ui_screens_login_LoginViewModel2;

      @KeepFieldType
      SessionViewModel com_digibrood_crmconnector_ui_screens_session_SessionViewModel2;

      @KeepFieldType
      SplashViewModel com_digibrood_crmconnector_ui_screens_splash_SplashViewModel2;

      @KeepFieldType
      RegisterViewModel com_digibrood_crmconnector_ui_screens_register_RegisterViewModel2;
    }
  }

  private static final class ViewModelCImpl extends CrmConnectorApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<CallPopupViewModel> callPopupViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<PermissionsViewModel> permissionsViewModelProvider;

    private Provider<RegisterViewModel> registerViewModelProvider;

    private Provider<SessionViewModel> sessionViewModelProvider;

    private Provider<SplashViewModel> splashViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.callPopupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.permissionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.registerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.sessionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.splashViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(7).put(LazyClassKeyProvider.com_digibrood_crmconnector_overlay_CallPopupViewModel, ((Provider) callPopupViewModelProvider)).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_dashboard_DashboardViewModel, ((Provider) dashboardViewModelProvider)).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_login_LoginViewModel, ((Provider) loginViewModelProvider)).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_permissions_PermissionsViewModel, ((Provider) permissionsViewModelProvider)).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_register_RegisterViewModel, ((Provider) registerViewModelProvider)).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_session_SessionViewModel, ((Provider) sessionViewModelProvider)).put(LazyClassKeyProvider.com_digibrood_crmconnector_ui_screens_splash_SplashViewModel, ((Provider) splashViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_digibrood_crmconnector_ui_screens_dashboard_DashboardViewModel = "com.digibrood.crmconnector.ui.screens.dashboard.DashboardViewModel";

      static String com_digibrood_crmconnector_overlay_CallPopupViewModel = "com.digibrood.crmconnector.overlay.CallPopupViewModel";

      static String com_digibrood_crmconnector_ui_screens_permissions_PermissionsViewModel = "com.digibrood.crmconnector.ui.screens.permissions.PermissionsViewModel";

      static String com_digibrood_crmconnector_ui_screens_session_SessionViewModel = "com.digibrood.crmconnector.ui.screens.session.SessionViewModel";

      static String com_digibrood_crmconnector_ui_screens_register_RegisterViewModel = "com.digibrood.crmconnector.ui.screens.register.RegisterViewModel";

      static String com_digibrood_crmconnector_ui_screens_login_LoginViewModel = "com.digibrood.crmconnector.ui.screens.login.LoginViewModel";

      static String com_digibrood_crmconnector_ui_screens_splash_SplashViewModel = "com.digibrood.crmconnector.ui.screens.splash.SplashViewModel";

      @KeepFieldType
      DashboardViewModel com_digibrood_crmconnector_ui_screens_dashboard_DashboardViewModel2;

      @KeepFieldType
      CallPopupViewModel com_digibrood_crmconnector_overlay_CallPopupViewModel2;

      @KeepFieldType
      PermissionsViewModel com_digibrood_crmconnector_ui_screens_permissions_PermissionsViewModel2;

      @KeepFieldType
      SessionViewModel com_digibrood_crmconnector_ui_screens_session_SessionViewModel2;

      @KeepFieldType
      RegisterViewModel com_digibrood_crmconnector_ui_screens_register_RegisterViewModel2;

      @KeepFieldType
      LoginViewModel com_digibrood_crmconnector_ui_screens_login_LoginViewModel2;

      @KeepFieldType
      SplashViewModel com_digibrood_crmconnector_ui_screens_splash_SplashViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.digibrood.crmconnector.overlay.CallPopupViewModel 
          return (T) new CallPopupViewModel(singletonCImpl.contactRepositoryProvider.get());

          case 1: // com.digibrood.crmconnector.ui.screens.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(singletonCImpl.deviceRepositoryProvider.get(), singletonCImpl.statsRepositoryProvider.get(), singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.callRepositoryProvider.get(), singletonCImpl.recordingRepositoryProvider.get(), singletonCImpl.connectivityObserverProvider.get(), singletonCImpl.syncManagerProvider.get(), singletonCImpl.syncControllerProvider.get(), singletonCImpl.permissionManagerProvider.get(), singletonCImpl.callLogReaderProvider.get(), singletonCImpl.crashReporterProvider.get(), singletonCImpl.deviceInfoProvider.get(), singletonCImpl.securePrefsProvider.get());

          case 2: // com.digibrood.crmconnector.ui.screens.login.LoginViewModel 
          return (T) new LoginViewModel(singletonCImpl.authRepositoryProvider.get(), singletonCImpl.startDestinationProvider.get(), singletonCImpl.securePrefsProvider.get());

          case 3: // com.digibrood.crmconnector.ui.screens.permissions.PermissionsViewModel 
          return (T) new PermissionsViewModel(singletonCImpl.permissionManagerProvider.get(), singletonCImpl.startDestinationProvider.get());

          case 4: // com.digibrood.crmconnector.ui.screens.register.RegisterViewModel 
          return (T) new RegisterViewModel(singletonCImpl.deviceRepositoryProvider.get(), singletonCImpl.securePrefsProvider.get());

          case 5: // com.digibrood.crmconnector.ui.screens.session.SessionViewModel 
          return (T) new SessionViewModel(singletonCImpl.securePrefsProvider.get());

          case 6: // com.digibrood.crmconnector.ui.screens.splash.SplashViewModel 
          return (T) new SplashViewModel(singletonCImpl.securePrefsProvider.get(), singletonCImpl.brandingRepositoryProvider.get(), singletonCImpl.deviceRepositoryProvider.get(), singletonCImpl.startDestinationProvider.get(), singletonCImpl.syncManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CrmConnectorApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CrmConnectorApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectSyncForegroundService(SyncForegroundService syncForegroundService) {
      injectSyncForegroundService2(syncForegroundService);
    }

    private SyncForegroundService injectSyncForegroundService2(SyncForegroundService instance) {
      SyncForegroundService_MembersInjector.injectNotificationHelper(instance, singletonCImpl.notificationHelperProvider.get());
      SyncForegroundService_MembersInjector.injectSyncController(instance, singletonCImpl.syncControllerProvider.get());
      SyncForegroundService_MembersInjector.injectConnectivity(instance, singletonCImpl.connectivityObserverProvider.get());
      SyncForegroundService_MembersInjector.injectDeviceRepository(instance, singletonCImpl.deviceRepositoryProvider.get());
      SyncForegroundService_MembersInjector.injectScheduler(instance, singletonCImpl.syncSchedulerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends CrmConnectorApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<SecurePrefs> securePrefsProvider;

    private Provider<HttpLoggingInterceptor> provideLoggingInterceptorProvider;

    private Provider<OkHttpClient> provideRefreshOkHttpClientProvider;

    private Provider<Moshi> provideMoshiProvider;

    private Provider<Retrofit> provideRefreshRetrofitProvider;

    private Provider<CrmApiService> provideRefreshApiServiceProvider;

    private Provider<OkHttpClient> provideMainOkHttpClientProvider;

    private Provider<Retrofit> provideMainRetrofitProvider;

    private Provider<CrmApiService> provideCrmApiServiceProvider;

    private Provider<DeviceInfoProvider> deviceInfoProvider;

    private Provider<ConnectivityObserver> connectivityObserverProvider;

    private Provider<DeviceRepository> deviceRepositoryProvider;

    private Provider<SettingsRepository> settingsRepositoryProvider;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<CallLogReader> callLogReaderProvider;

    private Provider<RecordingScanner> recordingScannerProvider;

    private Provider<OkHttpClient> provideUploadOkHttpClientProvider;

    private Provider<RecordingRepository> recordingRepositoryProvider;

    private Provider<CallRepository> callRepositoryProvider;

    private Provider<ContactReader> contactReaderProvider;

    private Provider<ContactRepository> contactRepositoryProvider;

    private Provider<SyncController> syncControllerProvider;

    private Provider<HeartbeatWorker_AssistedFactory> heartbeatWorker_AssistedFactoryProvider;

    private Provider<RecordingUploadWorker_AssistedFactory> recordingUploadWorker_AssistedFactoryProvider;

    private Provider<SyncWorker_AssistedFactory> syncWorker_AssistedFactoryProvider;

    private Provider<CrashReporter> crashReporterProvider;

    private Provider<WorkManager> provideWorkManagerProvider;

    private Provider<SyncScheduler> syncSchedulerProvider;

    private Provider<PermissionManager> permissionManagerProvider;

    private Provider<SyncManager> syncManagerProvider;

    private Provider<StatsRepository> statsRepositoryProvider;

    private Provider<AuthRepository> authRepositoryProvider;

    private Provider<StartDestinationProvider> startDestinationProvider;

    private Provider<BrandingRepository> brandingRepositoryProvider;

    private Provider<NotificationHelper> notificationHelperProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);

    }

    private DynamicBaseUrlInterceptor dynamicBaseUrlInterceptor() {
      return new DynamicBaseUrlInterceptor(securePrefsProvider.get());
    }

    private AuthInterceptor authInterceptor() {
      return new AuthInterceptor(securePrefsProvider.get());
    }

    private TokenAuthenticator tokenAuthenticator() {
      return new TokenAuthenticator(securePrefsProvider.get(), provideRefreshApiServiceProvider);
    }

    private CallDao callDao() {
      return DatabaseModule_ProvideCallDaoFactory.provideCallDao(provideDatabaseProvider.get());
    }

    private RecordingDao recordingDao() {
      return DatabaseModule_ProvideRecordingDaoFactory.provideRecordingDao(provideDatabaseProvider.get());
    }

    private RemarkDao remarkDao() {
      return DatabaseModule_ProvideRemarkDaoFactory.provideRemarkDao(provideDatabaseProvider.get());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return MapBuilder.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>newMapBuilder(3).put("com.digibrood.crmconnector.worker.HeartbeatWorker", ((Provider) heartbeatWorker_AssistedFactoryProvider)).put("com.digibrood.crmconnector.worker.RecordingUploadWorker", ((Provider) recordingUploadWorker_AssistedFactoryProvider)).put("com.digibrood.crmconnector.worker.SyncWorker", ((Provider) syncWorker_AssistedFactoryProvider)).build();
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.securePrefsProvider = DoubleCheck.provider(new SwitchingProvider<SecurePrefs>(singletonCImpl, 6));
      this.provideLoggingInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HttpLoggingInterceptor>(singletonCImpl, 10));
      this.provideRefreshOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 9));
      this.provideMoshiProvider = DoubleCheck.provider(new SwitchingProvider<Moshi>(singletonCImpl, 11));
      this.provideRefreshRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 8));
      this.provideRefreshApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<CrmApiService>(singletonCImpl, 7));
      this.provideMainOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 5));
      this.provideMainRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 4));
      this.provideCrmApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<CrmApiService>(singletonCImpl, 3));
      this.deviceInfoProvider = DoubleCheck.provider(new SwitchingProvider<DeviceInfoProvider>(singletonCImpl, 12));
      this.connectivityObserverProvider = DoubleCheck.provider(new SwitchingProvider<ConnectivityObserver>(singletonCImpl, 13));
      this.deviceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DeviceRepository>(singletonCImpl, 2));
      this.settingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 14));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 16));
      this.callLogReaderProvider = DoubleCheck.provider(new SwitchingProvider<CallLogReader>(singletonCImpl, 17));
      this.recordingScannerProvider = DoubleCheck.provider(new SwitchingProvider<RecordingScanner>(singletonCImpl, 19));
      this.provideUploadOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 20));
      this.recordingRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RecordingRepository>(singletonCImpl, 18));
      this.callRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CallRepository>(singletonCImpl, 15));
      this.contactReaderProvider = DoubleCheck.provider(new SwitchingProvider<ContactReader>(singletonCImpl, 22));
      this.contactRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ContactRepository>(singletonCImpl, 21));
      this.syncControllerProvider = DoubleCheck.provider(new SwitchingProvider<SyncController>(singletonCImpl, 1));
      this.heartbeatWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<HeartbeatWorker_AssistedFactory>(singletonCImpl, 0));
      this.recordingUploadWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<RecordingUploadWorker_AssistedFactory>(singletonCImpl, 23));
      this.syncWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<SyncWorker_AssistedFactory>(singletonCImpl, 24));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.crashReporterProvider = DoubleCheck.provider(new SwitchingProvider<CrashReporter>(singletonCImpl, 25));
      this.provideWorkManagerProvider = DoubleCheck.provider(new SwitchingProvider<WorkManager>(singletonCImpl, 28));
      this.syncSchedulerProvider = DoubleCheck.provider(new SwitchingProvider<SyncScheduler>(singletonCImpl, 27));
      this.permissionManagerProvider = DoubleCheck.provider(new SwitchingProvider<PermissionManager>(singletonCImpl, 29));
      this.syncManagerProvider = DoubleCheck.provider(new SwitchingProvider<SyncManager>(singletonCImpl, 26));
      this.statsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<StatsRepository>(singletonCImpl, 30));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 31));
      this.startDestinationProvider = DoubleCheck.provider(new SwitchingProvider<StartDestinationProvider>(singletonCImpl, 32));
      this.brandingRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<BrandingRepository>(singletonCImpl, 33));
      this.notificationHelperProvider = DoubleCheck.provider(new SwitchingProvider<NotificationHelper>(singletonCImpl, 34));
    }

    @Override
    public void injectCrmConnectorApp(CrmConnectorApp crmConnectorApp) {
      injectCrmConnectorApp2(crmConnectorApp);
    }

    @Override
    public void injectBootReceiver(BootReceiver bootReceiver) {
      injectBootReceiver2(bootReceiver);
    }

    @Override
    public void injectCallReceiver(CallReceiver callReceiver) {
      injectCallReceiver2(callReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private CrmConnectorApp injectCrmConnectorApp2(CrmConnectorApp instance) {
      CrmConnectorApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      CrmConnectorApp_MembersInjector.injectCrashReporter(instance, crashReporterProvider.get());
      return instance;
    }

    private BootReceiver injectBootReceiver2(BootReceiver instance2) {
      BootReceiver_MembersInjector.injectPrefs(instance2, securePrefsProvider.get());
      BootReceiver_MembersInjector.injectSyncManager(instance2, syncManagerProvider.get());
      return instance2;
    }

    private CallReceiver injectCallReceiver2(CallReceiver instance3) {
      CallReceiver_MembersInjector.injectScheduler(instance3, syncSchedulerProvider.get());
      CallReceiver_MembersInjector.injectPrefs(instance3, securePrefsProvider.get());
      CallReceiver_MembersInjector.injectPermissionManager(instance3, permissionManagerProvider.get());
      CallReceiver_MembersInjector.injectCallRepository(instance3, callRepositoryProvider.get());
      return instance3;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.digibrood.crmconnector.worker.HeartbeatWorker_AssistedFactory 
          return (T) new HeartbeatWorker_AssistedFactory() {
            @Override
            public HeartbeatWorker create(Context appContext, WorkerParameters params) {
              return new HeartbeatWorker(appContext, params, singletonCImpl.syncControllerProvider.get());
            }
          };

          case 1: // com.digibrood.crmconnector.sync.SyncController 
          return (T) new SyncController(singletonCImpl.deviceRepositoryProvider.get(), singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.callRepositoryProvider.get(), singletonCImpl.recordingRepositoryProvider.get(), singletonCImpl.contactRepositoryProvider.get(), singletonCImpl.connectivityObserverProvider.get());

          case 2: // com.digibrood.crmconnector.data.repository.DeviceRepository 
          return (T) new DeviceRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.securePrefsProvider.get(), singletonCImpl.deviceInfoProvider.get(), singletonCImpl.connectivityObserverProvider.get());

          case 3: // com.digibrood.crmconnector.data.remote.api.CrmApiService 
          return (T) NetworkModule_ProvideCrmApiServiceFactory.provideCrmApiService(singletonCImpl.provideMainRetrofitProvider.get());

          case 4: // @com.digibrood.crmconnector.di.MainClient retrofit2.Retrofit 
          return (T) NetworkModule_ProvideMainRetrofitFactory.provideMainRetrofit(singletonCImpl.provideMainOkHttpClientProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 5: // @com.digibrood.crmconnector.di.MainClient okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideMainOkHttpClientFactory.provideMainOkHttpClient(new HttpsEnforcementInterceptor(), singletonCImpl.dynamicBaseUrlInterceptor(), singletonCImpl.authInterceptor(), singletonCImpl.tokenAuthenticator(), singletonCImpl.provideLoggingInterceptorProvider.get());

          case 6: // com.digibrood.crmconnector.data.prefs.SecurePrefs 
          return (T) new SecurePrefs(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // @com.digibrood.crmconnector.di.RefreshClient com.digibrood.crmconnector.data.remote.api.CrmApiService 
          return (T) NetworkModule_ProvideRefreshApiServiceFactory.provideRefreshApiService(singletonCImpl.provideRefreshRetrofitProvider.get());

          case 8: // @com.digibrood.crmconnector.di.RefreshClient retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRefreshRetrofitFactory.provideRefreshRetrofit(singletonCImpl.provideRefreshOkHttpClientProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 9: // @com.digibrood.crmconnector.di.RefreshClient okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideRefreshOkHttpClientFactory.provideRefreshOkHttpClient(new HttpsEnforcementInterceptor(), singletonCImpl.dynamicBaseUrlInterceptor(), singletonCImpl.provideLoggingInterceptorProvider.get());

          case 10: // okhttp3.logging.HttpLoggingInterceptor 
          return (T) NetworkModule_ProvideLoggingInterceptorFactory.provideLoggingInterceptor();

          case 11: // com.squareup.moshi.Moshi 
          return (T) NetworkModule_ProvideMoshiFactory.provideMoshi();

          case 12: // com.digibrood.crmconnector.util.DeviceInfoProvider 
          return (T) new DeviceInfoProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.digibrood.crmconnector.util.ConnectivityObserver 
          return (T) new ConnectivityObserver(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 14: // com.digibrood.crmconnector.data.repository.SettingsRepository 
          return (T) new SettingsRepository(singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.securePrefsProvider.get());

          case 15: // com.digibrood.crmconnector.data.repository.CallRepository 
          return (T) new CallRepository(singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.callDao(), singletonCImpl.securePrefsProvider.get(), singletonCImpl.callLogReaderProvider.get(), singletonCImpl.deviceInfoProvider.get(), singletonCImpl.recordingRepositoryProvider.get());

          case 16: // com.digibrood.crmconnector.data.local.AppDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 17: // com.digibrood.crmconnector.util.CallLogReader 
          return (T) new CallLogReader(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 18: // com.digibrood.crmconnector.data.repository.RecordingRepository 
          return (T) new RecordingRepository(singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.recordingDao(), singletonCImpl.recordingScannerProvider.get(), singletonCImpl.securePrefsProvider.get(), singletonCImpl.deviceInfoProvider.get(), singletonCImpl.provideUploadOkHttpClientProvider.get());

          case 19: // com.digibrood.crmconnector.util.RecordingScanner 
          return (T) new RecordingScanner(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 20: // @com.digibrood.crmconnector.di.UploadClient okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideUploadOkHttpClientFactory.provideUploadOkHttpClient(new HttpsEnforcementInterceptor(), singletonCImpl.provideLoggingInterceptorProvider.get());

          case 21: // com.digibrood.crmconnector.data.repository.ContactRepository 
          return (T) new ContactRepository(singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.remarkDao(), singletonCImpl.deviceInfoProvider.get(), singletonCImpl.contactReaderProvider.get());

          case 22: // com.digibrood.crmconnector.util.ContactReader 
          return (T) new ContactReader(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 23: // com.digibrood.crmconnector.worker.RecordingUploadWorker_AssistedFactory 
          return (T) new RecordingUploadWorker_AssistedFactory() {
            @Override
            public RecordingUploadWorker create(Context appContext2, WorkerParameters params2) {
              return new RecordingUploadWorker(appContext2, params2, singletonCImpl.syncControllerProvider.get());
            }
          };

          case 24: // com.digibrood.crmconnector.worker.SyncWorker_AssistedFactory 
          return (T) new SyncWorker_AssistedFactory() {
            @Override
            public SyncWorker create(Context appContext3, WorkerParameters params3) {
              return new SyncWorker(appContext3, params3, singletonCImpl.syncControllerProvider.get());
            }
          };

          case 25: // com.digibrood.crmconnector.util.CrashReporter 
          return (T) new CrashReporter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 26: // com.digibrood.crmconnector.sync.SyncManager 
          return (T) new SyncManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.syncSchedulerProvider.get(), singletonCImpl.permissionManagerProvider.get());

          case 27: // com.digibrood.crmconnector.worker.SyncScheduler 
          return (T) new SyncScheduler(singletonCImpl.provideWorkManagerProvider.get(), singletonCImpl.settingsRepositoryProvider.get());

          case 28: // androidx.work.WorkManager 
          return (T) AppModule_ProvideWorkManagerFactory.provideWorkManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 29: // com.digibrood.crmconnector.util.PermissionManager 
          return (T) new PermissionManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 30: // com.digibrood.crmconnector.data.repository.StatsRepository 
          return (T) new StatsRepository(singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.callDao(), singletonCImpl.recordingDao(), singletonCImpl.securePrefsProvider.get());

          case 31: // com.digibrood.crmconnector.data.repository.AuthRepository 
          return (T) new AuthRepository(singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.securePrefsProvider.get());

          case 32: // com.digibrood.crmconnector.ui.navigation.StartDestinationProvider 
          return (T) new StartDestinationProvider(singletonCImpl.securePrefsProvider.get(), singletonCImpl.permissionManagerProvider.get());

          case 33: // com.digibrood.crmconnector.data.repository.BrandingRepository 
          return (T) new BrandingRepository(singletonCImpl.provideCrmApiServiceProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.securePrefsProvider.get());

          case 34: // com.digibrood.crmconnector.service.NotificationHelper 
          return (T) new NotificationHelper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
