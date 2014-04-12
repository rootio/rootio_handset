package org.rootio.activities.synchronization;

import org.rootio.radioClient.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class SynchronizationLogDownloadActivity extends Activity {
private int offset = 0;
private int limit = 100;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.synchronization_log_download);
		ListView listView = (ListView)this.findViewById(R.id.synchronization_log_download_lv);
	    SynchronizationLogDownloadAdapter adapter = new SynchronizationLogDownloadAdapter(this,this.offset, this.limit);
	    listView.setAdapter(adapter);
	    this.setTitle("Download Backlog");
		
	}
	
	
}
