package org.rootio.tools.radio;

import org.rootio.tools.media.Program;

public class ProgramSlot implements Comparable<ProgramSlot> {

	private Program program;
	private int scheduledIndex;
	private int runState;

	ProgramSlot(Program program, int scheduledIndex) {
		this.program = program;
		this.scheduledIndex = scheduledIndex;
		this.runState = 0;
	}

	/**
	 * Gets the index of the scheduled Event time
	 * @return
	 */
	public int getScheduledIndex() {
		return this.scheduledIndex;
	}

	/**
	 * Gets the program with which this program slot is identified
	 * @return The Program for this program slot
	 */
	public Program getProgram() {
		return this.program;
	}

	/**
	 * Updates the state of this program slot to indicate that it is now running
	 */
	public void setRunning() {
		this.runState = 1;
	}

	/**
	 * Updates the state of this program to indicate that it has finished running
	 */
	public void setFinishedRunning() {
		this.runState = 2;
	}

	/**
	 * Gets the state of this program slot
	 * @return integer representing the state of this program slot
	 */
	public int getRunState() {
		return this.runState;
	}

	@Override
	public int compareTo(ProgramSlot another) {
		return this.program.getEventTimes()[this.getScheduledIndex()]
				.getScheduledDate().compareTo(another.getProgram().getEventTimes()[another.getScheduledIndex()]
						.getScheduledDate());
	}

}
