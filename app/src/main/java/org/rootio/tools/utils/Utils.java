package org.rootio.tools.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.persistence.DBAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@SuppressLint("SimpleDateFormat")
public class Utils {

    private static Handler handler = new Handler();

    public static void setContext(Context context) {
    }


    public static Long getEventTimeId(Context parent, long programId, Date scheduleDate, int duration) {
        String tableName = "eventtime";
        String[] columns = new String[]{"id"};
        String whereClause = "programid = ? and duration = ? and scheduledate = ?";
        String[] whereArgs = new String[]{String.valueOf(programId), String.valueOf(duration), Utils.getDateString(scheduleDate, "yyyy-MM-dd HH:mm:ss")};
        //DBAgent dbAgent = new DBAgent(parent);
        String[][] results = DBAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
        return results.length > 0 ? Long.parseLong(results[0][0]) : 0l;
    }

    public static void toastOnScreen(final String message, final Context context) {
        Runnable toaster = new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                toast.show();
            }
        };
        Utils.handle(toaster);
    }

    public static void warnOnScreen(Activity triggerActivity, String message, DialogInterface.OnDismissListener listener) {
        new AlertDialog.Builder(triggerActivity).setIcon(R.drawable.attention).setOnDismissListener(listener).setTitle("Warning").setMessage(message).setNeutralButton("Close", null).show();
    }

    public static void warnOnScreen(Activity triggerActivity, String message) {
        new AlertDialog.Builder(triggerActivity).setIcon(R.drawable.attention).setTitle("Warning").setMessage(message).setNeutralButton("Close", null).show();
    }


    public static void doNotification(ContextWrapper contextWrapper, String title, String content, int icon) {
        Utils.doNotification(contextWrapper, title, content, icon, true, null);
    }

    public static void doNotification(ContextWrapper contextWrapper, String title, String content) {
        Utils.doNotification(contextWrapper, title, content, R.drawable.ic_launcher);
    }


    public static void doNotification(ContextWrapper contextWrapper, String title, String content, int icon, boolean autoCancel, PendingIntent contentIntent) {
        Utils.doNotification(contextWrapper, title, content, icon, autoCancel, contentIntent, null);
    }

    @SuppressLint("NewApi")
    public static void doNotification(ContextWrapper contextWrapper, String title, String content, int icon, boolean autoCancel, PendingIntent contentIntent, NotificationAction[] notificationActions) {

        Notification notification = getNotification(contextWrapper, title, content, icon, autoCancel, contentIntent, notificationActions);
        NotificationManager notificationManager = (NotificationManager) contextWrapper.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    public static Notification getNotification(ContextWrapper contextWrapper, String title, String content, int icon, boolean autoCancel, PendingIntent contentIntent, NotificationAction[] notificationActions) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(contextWrapper);
        notificationBuilder = notificationBuilder.setContentTitle(title);
        notificationBuilder = notificationBuilder.setContentText(content);
        notificationBuilder = notificationBuilder.setContentIntent(contentIntent);
        notificationBuilder.setSmallIcon(icon);
        notificationBuilder = notificationBuilder.setAutoCancel(autoCancel);

        for (int i = 0; notificationActions != null && i < notificationActions.length && i < 2; i++) {
            notificationBuilder = notificationBuilder.addAction(notificationActions[i].getIconId(), notificationActions[i].getTitle(), notificationActions[i].getPendingIntent());
        }
        return notificationBuilder.build();
    }

    public static void handle(Runnable runnable) {
        Utils.handler.post(runnable);
    }


    public static String getCurrentDateAsString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date now = Calendar.getInstance().getTime();
        try {
            return sdf.format(now);
        } catch (Exception ex) {
            return "";
        }
    }

    public static Date getDateFromString(String input, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(input);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getDateString(Date input, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.format(input);
        } catch (Exception ex) {
            return null;
        }
    }

    public static int parseIntFromString(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static long parseLongFromString(String input) {
        try {
            return Long.parseLong(input);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static double parseDoubleFromString(String input) {
        try {
            return Double.parseDouble(input);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static InetAddress parseInetAddressFromString(String input) {
        try {
            InetAddress address = InetAddress.getByName(input);
            return address;
        } catch (Exception ex) {
            return null;
        }
    }

    public static JSONObject getJSONFromFile(Context context, String fileName) {
        FileInputStream input = null;
        try {
            File jsonFile = new File(fileName);
            input = new FileInputStream(jsonFile);
            byte[] buffer = new byte[1024];
            input.read(buffer);
            return new JSONObject(new String(buffer));
        } catch (Exception ex) {
            Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "NullPointerException(CallAuthenticator.isWhiteListed)" : ex.getMessage());
            return null;
        } finally {
            try {
                input.close();
            } catch (Exception ex) {
                // log the exception
            }
        }

    }

    public static void savePreferences(ContentValues values, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : values.keySet()) {
            Class cls = values.get(key).getClass();
            if (cls == String.class) {
                editor.putString(key, values.getAsString(key));
            } else if (cls == Integer.class) {
                editor.putInt(key, values.getAsInteger(key));
            } else if (cls == Boolean.class) {
                editor.putBoolean(key, values.getAsBoolean(key));
            } else if (cls == Long.class) {
                editor.putLong(key, values.getAsLong(key));
            } else if (cls == Float.class) {
                editor.putFloat(key, values.getAsFloat(key));
            }
        }
        editor.commit();
    }

    public static Object getPreference(String key, Class cls, Context context)
    {
        try{
        SharedPreferences prefs = context.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        if(prefs != null) {
            if (cls == String.class) {
                return prefs.getString(key, null);
            } else if (cls == int.class) {
                return prefs.getInt(key, 0);
            } else if (cls == boolean.class) {
                return prefs.getBoolean(key, false);
            } else if (cls == long.class) {
                return prefs.getLong(key, 0l);
            } else if (cls == float.class) {
                return prefs.getFloat(key, 0f);
            }
        }}
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        catch (StackOverflowError er)
        {
            er.printStackTrace();
        }
        return null;
    }

    public static String doPostHTTP(String httpUrl, String data) {
        URL url;
        try {
            url = new URL(httpUrl);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.connect();
            OutputStream outstr = httpUrlConnection.getOutputStream();
            //writeToFile(data);
            outstr.write(data.getBytes());
            outstr.flush();
            InputStream instr = httpUrlConnection.getInputStream();
            StringBuilder response = new StringBuilder();
            while (true) {
                int tmp = instr.read();
                if (tmp < 0) {
                    break;
                }
                response.append((char) tmp);
            }
            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, Object> doDetailedPostHTTP(String httpUrl, String data) {
        URL url;
        Long then  = Calendar.getInstance().getTimeInMillis();
        HashMap<String, Object> responseData = new HashMap<>();
        try {
            url = new URL(httpUrl);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.connect();
            OutputStream outstr = httpUrlConnection.getOutputStream();
            //writeToFile(data);
            outstr.write(data.getBytes());
            outstr.flush();
            InputStream instr = httpUrlConnection.getInputStream();
            StringBuilder response = new StringBuilder();
            while (true) {
                int tmp = instr.read();
                if (tmp < 0) {
                    break;
                }
                response.append((char) tmp);
            }
            responseData.put("response", response.toString());
            responseData.put("duration", Calendar.getInstance().getTimeInMillis() - then); //ChronoUnit.MICROS.between(dt, LocalDate.now()));
            responseData.put("responseCode", httpUrlConnection.getResponseCode());
            responseData.put("length", httpUrlConnection.getContentLength());
            responseData.put("url", httpUrlConnection.getURL());
            return responseData;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check to see if this phone is connected to a station in the cloud. This is done by looking for config files that are created when a station is connected
     *
     * @return True if connected, false if not connected
     */
    public static boolean isConnectedToStation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        return prefs != null && prefs.contains("station_information");
     }

     public static long logEvent(Context context, EventCategory category, EventAction action, String argument)
     {
          try {
             ContentValues values = new ContentValues();
             values.put("category", category.name());
             values.put("argument", argument);
             values.put("event", action.name());
             values.put("eventdate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
             return DBAgent.saveData("activitylog", null, values);
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
         }
         return 0;
     }

    public static void writeToFile(Context ctx, String data){
        File fl = new File(ctx.getExternalFilesDir(null), Utils.getCurrentDateAsString("YYYYMMDD_HmS")+ "_log.txt");
         FileWriter fwr = null;
         try {
             fwr = new FileWriter(fl);

         fwr.write(data);
         fwr.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

     public enum EventCategory
    {
        MEDIA, SERVICES, SYNC, SMS, CALL, SIP_CALL, DATA_NETWORK
    }

    public enum EventAction
    {
        ON, OFF, PAUSE, STOP, START, SEND, RECEIVE, REGISTRATION, RINGING, UMOUNT, LOAD, PREPARE, ERROR;
    }
}
