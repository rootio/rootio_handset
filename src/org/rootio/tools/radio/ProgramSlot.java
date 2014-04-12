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

	public int getScheduledIndex() {
		return this.scheduledIndex;
	}

	public Program getProgram() {
		return this.program;
	}

	public void setRunning() {
		this.runState = 1;
	}

	public void setFinishedRunning() {
		this.runState = 2;
	}

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
