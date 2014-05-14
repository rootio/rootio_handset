package org.rootio.tools.media;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ProgramManager {

	enum ProgramActionType{Jingle, Media, Music, Stream, Call};
	private Context parent;
	private JingleManager jingleManager;
	private PlayList playlist;
	private Program program;
	private ArrayList<ProgramAction> programActions;
	private AlertHandler alertHandler;
	
	ProgramManager(Context parent, Program program)
	{
		this.parent = parent;
		this.program = program;
		this.alertHandler = new AlertHandler();
	}
	
	public void runProgram()
	{
		this.programActions = fetchProgramActions(this.program);
		this.setupAlertReceiver(this.program, alertHandler, programActions);
	}
	
	public void pause()
	{
		this.alertHandler.pauseProgramAction();
	}
	
	public void play()
	{
		this.alertHandler.playProgramAction();
	}
	
	public void stop()
	{
		this.alertHandler.stopProgramAction();
	}
	
	private void setupAlertReceiver(Program program, AlertHandler alertHandler, ArrayList<ProgramAction> programActions)
	{
		IntentFilter intentFilter = new IntentFilter();
		this.parent.registerReceiver(alertHandler, intentFilter);
		AlarmManager am = (AlarmManager)this.parent.getSystemService(Context.ALARM_SERVICE);
		for(int i= 0; i < programActions.size(); i++)
		{
			intentFilter.addAction(String.valueOf(i));	
		}
		
		this.parent.registerReceiver(new AlertHandler(), intentFilter);
		
		for(int i = 0; i < programActions.size(); i++)
		{
			Intent intent = new Intent(String.valueOf(i));
			intent.putExtra("index", i);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this.parent, 0, intent,0);
			am.set(0, this.getStartTime(program, programActions.get(i).getStartTime()), pendingIntent);
		}
		
	}
	
	private long getStartTime(Program program, Date programActionDate)
	{
		Date baseDate = program.getEventTimes()[program.getScheduledIndex()].getScheduledDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(baseDate);
		calendar.add(Calendar.HOUR_OF_DAY, programActionDate.getHours());
		calendar.add(Calendar.MINUTE, programActionDate.getMinutes());
		calendar.add(Calendar.SECOND, programActionDate.getSeconds());
		return calendar.getTimeInMillis();
	}
	
	private ArrayList<ProgramAction> fetchProgramActions(Program program)
	{
		ArrayList<ProgramAction> programActions = new ArrayList<ProgramAction>();
		long id = program.getEventTimes()[program.getScheduledIndex()].getId();
		String tableName = "programaction";
		String[] columns = new String[]{"starttime", "duration","programactiontypeid", "argument"};
		String whereClause = "eventtimeid = ?";
		String[] whereArgs = new String[]{String.valueOf(id)};
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
		for(String[] result : results)
		{
			ProgramAction programAction = new ProgramAction(Utils.parseIntFromString(result[1]), Utils.getDateFromString(result[0], "HH:mm:ss"), this.getProgramActionType(Utils.parseIntFromString(result[2])), result[3]);
			programActions.add(programAction);
		}
		return programActions;
	}
	
	private ProgramActionType getProgramActionType(int id)
	{
		String tableName = "programactiontype";
		String[] columns = new String[]{"title"};
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{String.valueOf(id)};
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
		try
		{
			return ProgramActionType.valueOf(results[0][0]);
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	class ProgramAction
	{
		private int duration;
		private Date time;
		private String argument;
		private ProgramActionType programActionType;
		
		ProgramAction(int duration, Date time, ProgramActionType programActionType, String argument)
		{
			this.duration = duration;
			this.time = time;
			this.programActionType = programActionType;
			this.argument = argument;
		}
		
		void run()
		{
			switch(this.programActionType)
			{
			case Media:
			case Music:
			case Stream:
				ProgramManager.this.playlist = new PlayList(ProgramManager.this.parent, this.argument, this.programActionType);
				ProgramManager.this.playlist.load();
				ProgramManager.this.playlist.play();
				break;
			case Jingle:
				ProgramManager.this.jingleManager = new JingleManager(ProgramManager.this.parent, ProgramManager.this.program);
				ProgramManager.this.jingleManager.playJingle();
				break;
			case Call:
				break;
			default:
				break;
			}
		}
		
		void play()
		{
			switch(this.programActionType)
			{
			case Media:
			case Music:
			case Stream:
				ProgramManager.this.playlist.play();
				break;
			case Jingle:
				ProgramManager.this.jingleManager.play();
				break;
			case Call:
				break;
			}
		}
		
		void pause()
		{
			switch(this.programActionType)
			{
			case Media:
			case Music:
			case Stream:
				ProgramManager.this.playlist.pause();
				break;
			case Jingle:
				ProgramManager.this.jingleManager.pause();
				break;
			case Call:
				break;
			}
		}
		
		void stop()
		{
			switch(this.programActionType)
			{
			case Media:
			case Music:
			case Stream:
				ProgramManager.this.playlist.stop();
				break;
			case Jingle:
				ProgramManager.this.jingleManager.stop();
				break;
			case Call:
				break;
			}
		}
		
		Date getStartTime()
		{
			return this.time;
		}
		
		int getDuration()
		{
			return this.duration;
		}
	}
	
	class AlertHandler extends BroadcastReceiver
	{
		private ProgramAction runningProgramAction;
		private int currentIndex = 1000000; //no initial value results in 0

		@Override
		public void onReceive(Context context, Intent intent) {
			this.runProgramAction(intent, ProgramManager.this.program, ProgramManager.this.programActions);
		}
		
		private void runProgramAction(Intent intent, Program program, ArrayList<ProgramAction> programActions)
		{
			
			int index = intent.getIntExtra("index", 0);
			if (index != currentIndex) {
				currentIndex = index;

				if (this.runningProgramAction != null) {
					this.runningProgramAction.stop();
				}
				if (!isExpired(program, programActions.get(index))) {
					programActions.get(index).run();
					this.runningProgramAction = programActions.get(index);
				} 
			}
			
		}
		
		private boolean isExpired(Program program, ProgramAction programAction)
		{
			Date startDate = program.getEventTimes()[program.getScheduledIndex()].getScheduledDate();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			calendar.add(Calendar.HOUR_OF_DAY, programAction.getStartTime().getHours());
			calendar.add(Calendar.MINUTE, programAction.getStartTime().getMinutes());
			calendar.add(Calendar.SECOND, programAction.getStartTime().getSeconds());
			
			//add the duration in minutes
			calendar.add(Calendar.MINUTE, programAction.getDuration());
			
			//compare if the time for the scheduled event is already past
			Calendar calendar2 = Calendar.getInstance();
			return calendar2.after(calendar);
		}
		
		private void pauseProgramAction()
		{
			if(this.runningProgramAction != null)
			{
				this.runningProgramAction.pause();
			}
		}
		
		private void playProgramAction()
		{
			if(this.runningProgramAction != null)
			{
				this.runningProgramAction.play();
			}
		}
		
		private void stopProgramAction()
		{
			if (this.runningProgramAction != null) {
				this.runningProgramAction.stop();
			}
		}
	}

	public PlayList getPlayList() {
		return this.playlist;
	}

	
}
