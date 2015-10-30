package test;

import java.util.Vector;

public abstract class AutoTask {
	private boolean complete;
	int[] parents;
	
	public AutoTask(int[] parents, String[] args) {
		this.parents = parents;
	}
	
	public abstract void run();
	
	public boolean complete() {
		return complete;
	}
	
	public static void runAll(AutoTask[] tasks) {
		f:for (AutoTask t : tasks) {
			for (int p : t.parents) 
				if (!tasks[p].complete()) continue f;
			t.run();
		}
	}
	
}
