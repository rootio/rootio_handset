package org.rootio.activities.diagnostics;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class FrequencyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.frequencies);
        this.setTitle("Frequencies");
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        this.loadFrequencies();
    }


    private void loadFrequencies() {
        try {
        JSONObject frequencyInformation = new JSONObject((String)Utils.getPreference("frequencies", String.class, this));
         if (frequencyInformation.has("diagnostics")) {

                ((TextView) this.findViewById(R.id.diagnostic_frequency_tv)).setText(this.getAppropriateText(frequencyInformation.getJSONObject("diagnostics").getInt("interval")));
            }
            if (frequencyInformation.has("synchronization")) {
                ((TextView) this.findViewById(R.id.sync_frequency_tv)).setText(this.getAppropriateText(frequencyInformation.getJSONObject("synchronization").getInt("interval")));
            }
        } catch (Exception e) {
            Log.e(TAG, "loadFrequencies: " + e.getMessage(),e);
        }

    }

    private String getAppropriateText(int seconds) {
        return String.format("After %s hrs, %smins, %s secs", seconds / 3600, (seconds % 3600) / 60, (seconds % 3600) % 60);
    }
}
