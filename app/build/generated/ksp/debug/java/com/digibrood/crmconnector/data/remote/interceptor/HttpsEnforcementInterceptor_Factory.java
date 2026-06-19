package com.digibrood.crmconnector.data.remote.interceptor;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class HttpsEnforcementInterceptor_Factory implements Factory<HttpsEnforcementInterceptor> {
  @Override
  public HttpsEnforcementInterceptor get() {
    return newInstance();
  }

  public static HttpsEnforcementInterceptor_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HttpsEnforcementInterceptor newInstance() {
    return new HttpsEnforcementInterceptor();
  }

  private static final class InstanceHolder {
    private static final HttpsEnforcementInterceptor_Factory INSTANCE = new HttpsEnforcementInterceptor_Factory();
  }
}
