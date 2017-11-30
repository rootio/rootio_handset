package org.rootio.tools.telephony;

import java.io.File;

import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

public class CallRecorder {
    private Context parent;
    private MediaRecorder mediaRecorder;

    public CallRecorder(Context parent) {
        this.parent = parent;
        this.mediaRecorder = new MediaRecorder();
    }

    private String getFileName() {
        String datePart = Utils.getCurrentDateAsString("yyyyMMddHHmmss");
        String filePath = "/mnt/extSdCard/calls";
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return String.format("%s/%s.3gp", filePath, datePart);
    }

    public void startRecording() {

        String fileName = this.getFileName();
        if (fileName != null) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //mediaRecorder.setAudioChannels(1); // do mono
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mediaRecorder.setOutputFile(fileName);
            try {
                mediaRecorder.prepare();
            } catch (Exception ex) {
                Log.e(this.parent.getString(R.string.app_name), ex.getMessage() ==
                        null ? "Null pointer exception(CallRecorder.startRecording)" :
                        ex.getMessage());
            }
            mediaRecorder.start();
        }

    }

    public void stopRecording() {
        mediaRecorder.stop();
    }
}
