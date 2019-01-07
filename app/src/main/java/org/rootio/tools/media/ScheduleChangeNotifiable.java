package org.rootio.tools.media;

/**
 * Created by Jude Mukundane on 7/5/2017.
 */

public interface ScheduleChangeNotifiable {
    void notifyScheduleChange(boolean shouldRestart);
}
