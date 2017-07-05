/**
 * 
 */
package org.rootio.tools.media;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 *
 */
public interface ScheduleNotifiable {

	
	void runProgram(int currentIndex);

	void stopProgram(Integer index);

	boolean isExpired(int index);
}
