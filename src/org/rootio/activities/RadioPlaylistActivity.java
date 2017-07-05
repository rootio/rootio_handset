package org.rootio.activities;

import java.util.ArrayList;

import org.rootio.radioClient.R;
import org.rootio.services.Notifiable;
import org.rootio.services.ProgramService;
import org.rootio.services.ServiceConnectionAgent;
import org.rootio.tools.radio.ProgramSlot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class RadioPlaylistActivity extends Activity implements Notifiable{

	private ServiceConnectionAgent programServiceConnection;
	private ProgramService programService;
	private ArrayList<ProgramSlot> programSlots;
	private int serviceId, programIndex;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.programIndex = this.getIntent().getIntExtra("index", 0);
		this.setContentView(R.layout.station_activity_playlist);
		this.serviceId = 4;
		this.bindToService(this.serviceId); //service id for program service is 4
	}
	
	/**
	 * Binds to the program service to obtain information about the currently running program
	 * @param serviceId the ID of the service to which to bind. 
	 */
	private void bindToService(int serviceId)
	{
		Intent intent = new Intent(this, ProgramService.class);
		programServiceConnection = new ServiceConnectionAgent(this, serviceId);
		this.bindService(intent, programServiceConnection, Context.BIND_AUTO_CREATE); //Async call, wait for call back to notifyServiceConnection()
	}
	
	
	@Override
	public void notifyServiceConnection(int serviceId) {
		programService = (ProgramService)programServiceConnection.getService();
		programSlots = programService.getProgramSlots();
		ListView stationActivityPlaylistListView = (ListView)this.findViewById(R.id.station_activity_playlist_lv);
		stationActivityPlaylistListView.setAdapter(new RadioPlaylistActivityAdapter(this.programSlots.get(this.programIndex).getProgram()));
		this.setTitle(this.programSlots.get(this.programIndex).getProgram().getTitle());
	}

	@Override
	public void notifyServiceDisconnection(int serviceId) {
		this.bindToService(this.serviceId);
		
	}	
	
	@Override
	public void onStop()
	{
		try
		{
		this.unbindService(programServiceConnection);
		}
		catch(Exception ex)//may not be bound
		{
			Log.e(this.getString(R.string.app_name), ex.getMessage());
		}
	}
}
