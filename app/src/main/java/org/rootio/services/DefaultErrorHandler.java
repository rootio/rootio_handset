package org.rootio.services;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.rootio.handset.R;

/**
 * Created by Jude Mukundane on 02/12/2017.
 */

public class DefaultErrorHandler extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                                                      @Override
                                                      public void uncaughtException(Thread thread, Throwable throwable) {
//if only we knew where this is coming from. For now we will restart all services
                                                          try {
                                                              startServices();
                                                          }
                                                          catch (Exception e)
                                                          {
                                                              Log.e(DefaultErrorHandler.this.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[DefaultErrorHandler]" : e.getMessage());
                                                          }
                                                      }

                                                      public void startServices() {
                                                          for (int serviceId : new int[]{1, 2, 3, 4, 5}) // only vitals
                                                          {
                                                              //ServiceState serviceState = new ServiceState(context, serviceId);
                                                              // if(serviceState.getServiceState() > 0)//service was started
                                                              // {
                                                              Intent intent = this.getIntentToLaunch(DefaultErrorHandler.this, serviceId);
                                                              DefaultErrorHandler.this.stopService(intent);
                                                              DefaultErrorHandler.this.startService(intent);
                                                              // }
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
                                                              case 1: // telephony service
                                                                  intent = new Intent(context, TelephonyService.class);
                                                                  break;
                                                              case 2: // SMS service
                                                                  intent = new Intent(context, SMSService.class);
                                                                  break;
                                                              case 3: // Diagnostic Service
                                                                  intent = new Intent(context, DiagnosticsService.class);
                                                                  break;
                                                              case 4: // Program Service
                                                                  intent = new Intent(context, ProgramService.class);
                                                                  break;
                                                              case 5: // Sync Service
                                                                  intent = new Intent(context, SynchronizationService.class);
                                                                  break;
                                                              case 6: // Discovery Service
                                                                  intent = new Intent(context, DiscoveryService.class);
                                                                  break;
                                                          }
                                                          return intent;
                                                      }

                                                  }
        );
    }
}
