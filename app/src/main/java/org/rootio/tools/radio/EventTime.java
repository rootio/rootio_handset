package org.rootio.tools.radio;

import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class EventTime {
    private Date scheduleDate;
    private int duration;
    private long programId;
    private Long id;
    private Context parent;
    private boolean isRepeat;

    /**
     * Constructor for the EventTime object
     *
     * @param dayOfWeek The day of the week on which this EventTime is scheduled
     * @param startTime The Time at which the EventTime is scheduled in format
     *                  <HH>h<i>
     * @param duration  The duration that this Eventtime is scheduled to last
     */
    public EventTime(Context parent, long programId, Date scheduleDate, int duration, boolean isRepeat) {
        this.parent = parent;
        this.isRepeat = isRepeat;
        this.programId = programId;
        this.scheduleDate = scheduleDate;
        this.duration = duration;
        this.id = Utils.getEventTimeId(this.parent, this.programId, this.scheduleDate, this.duration);
        if (this.id <= 0) {
            this.id = this.persist();
        }
    }

    /**
     * Gets the id of this EventTime object
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Gets the day of the week for this event progressing from Sunday as day 1
     * to Saturday as day 7
     *
     * @return The day of the week as an integer
     */
    public Date getScheduledDate() {
        return this.scheduleDate;
    }

    /**
     * Get the duration of this EventTime in seconds
     *
     * @return Duration of this EventTime in seconds as an integer
     */
    public int getDuration() {
        return this.duration;
    }

    public boolean isRepeat() {
        return this.isRepeat;
    }

    /**
     * Save this EventTime object to the Rootio database in case it is not yet
     * persisted
     *
     * @return Long id of the row saved in the Rootio database
     */
//    @org.jetbrains.annotations.NotNull
    private Long persist() {
        String tableName = "eventtime";
        ContentValues data = new ContentValues();
        data.put("programid", this.programId);
        data.put("scheduledate", Utils.getDateString(this.scheduleDate, "yyyy-MM-dd HH:mm:ss"));
        data.put("duration", this.duration);
        data.put("isrepeat", this.isRepeat);
        DBAgent agent = new DBAgent(this.parent);
        return agent.saveData(tableName, null, data);
    }
}
