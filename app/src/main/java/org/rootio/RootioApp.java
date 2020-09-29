package org.rootio;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.rootio.handset.R;
import org.rootio.services.DiagnosticsService;
import org.rootio.services.RadioService;
import org.rootio.services.SynchronizationService;

public class RootioApp extends Application {
    public static RootioApp instance;

    private static boolean inCall, inSIPCall;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                                                      @Override
                                                      public void uncaughtException(Thread thread, Throwable throwable) {
//if only we knew where this is coming from. For now we will restart all services
                                                          try {
                                                              startServices();
                                                          } catch (Exception e) {
                                                              Log.e(RootioApp.this.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[DefaultErrorHandler]" : e.getMessage());
                                                          }
                                                      }

                                                      public void startServices() {
                                                          for (int serviceId : new int[]{/*1, 2,*/ 3, 4, 5 /*, 6*/})
                                                          {
                                                              //ServiceState serviceState = new ServiceState(context, serviceId);
                                                              // if(serviceState.getServiceState() > 0)//service was started
                                                              // {
                                                              Intent intent = this.getIntentToLaunch(RootioApp.this, serviceId);
                                                              try {
                                                                  RootioApp.this.stopService(intent);
                                                              } catch (Exception ex) {
                                                                  Log.e(RootioApp.this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer[DefaultErrorHandler]" : ex.getMessage());
                                                              }

                                                              try {
                                                                  RootioApp.this.startForegroundService(intent);
                                                              } catch (Exception ex) {
                                                                  Log.e(RootioApp.this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer[DefaultErrorHandler]" : ex.getMessage());
                                                              }
                                                          }

                                                      }

                                                      /**
                                                       * Gets the intent to be used to launch the service with the specified
                                                       * serviceId
                                                       *
                                                       * @param context   The context to be used in creating the intent
                                                       * @param serviceId The ID of the service for which to create the intent
                                                       * @return
                                                       */
                                                      private Intent getIntentToLaunch(Context context, int serviceId) {
                                                          Intent intent = null;
                                                          switch (serviceId) {
                                                              case 3: // Diagnostic Service
                                                                  intent = new Intent(context, DiagnosticsService.class);
                                                                  break;
                                                              case 4: // Program Service
                                                                  intent = new Intent(context, RadioService.class);
                                                                  break;
                                                              case 5: // Sync Service
                                                                  intent = new Intent(context, SynchronizationService.class);
                                                                  break;
                                                         }
                                                          return intent;
                                                      }

                                                  }
        );
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static RootioApp getInstance() {
        return instance;
    }

    public static boolean isInCall() {
        return inCall;
    }

    public static void setInCall(boolean isInCall) {
        inCall = isInCall;
    }

    public static boolean isInSIPCall() {
        return inSIPCall;
    }

    public static void setInSIPCall(boolean isInSIPCall) {
        inSIPCall = isInSIPCall;
    }
}
