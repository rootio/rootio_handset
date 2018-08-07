package org.rootio.tools.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.persistence.DBAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        DBAgent dbAgent = new DBAgent(parent);
        String[][] results = dbAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(contextWrapper);
        notificationBuilder = notificationBuilder.setContentTitle(title);
        notificationBuilder = notificationBuilder.setContentText(content);
        notificationBuilder = notificationBuilder.setContentIntent(contentIntent);
        notificationBuilder.setSmallIcon(icon);
        notificationBuilder = notificationBuilder.setAutoCancel(autoCancel);

        for (int i = 0; notificationActions != null && i < notificationActions.length && i < 2; i++) {
            notificationBuilder = notificationBuilder.addAction(notificationActions[i].getIconId(), notificationActions[i].getTitle(), notificationActions[i].getPendingIntent());
        }
        Notification notification = notificationBuilder.build();
        NotificationManager notificationManager = (NotificationManager) contextWrapper.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
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

    /**
     * Breaks down the information in the JSON file for program and schedule information
     *
     * @param programDefinition The JSON program definition received from the cloud server
     */
    public static void saveJSONToFile(Context context, JSONObject json, String fileName) {
        FileOutputStream str = null;
        try {
            File whitelistFile = new File(fileName);
            str = new FileOutputStream(whitelistFile);
            str.write(json.toString().getBytes());
        } catch (Exception e) {
            Log.e(context.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[FrequencyHandler.processJSONObject]" : e.getMessage());
        } finally {
            try {
                str.close();
            } catch (Exception e) {
                Log.e(context.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[FrequencyHandler.processJSONObject]" : e.getMessage());
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

    /**
     * Check to see if this phone is connected to a station in the cloud. This is done by looking for config files that are created when a station is connected
     *
     * @return True if connected, false if not connected
     */
    public static boolean isConnectedToStation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        return prefs != null && !prefs.getAll().isEmpty();
     }
}
