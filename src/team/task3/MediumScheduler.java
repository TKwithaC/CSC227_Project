public class MediumScheduler {
	private static final double swapOutThreshold=90.0;
	private static final double swapInThreshold=70.0;
	

	public static boolean step(Machine machine) {
		if(!machine.mtsEnabled)
			return false;
		double currentMem=utilization(machine);
		if(currentMem>swapOutThreshold&&canSwap(machine)) {
			return swapOut(machine,currentMem);
		}
		else if(currentMem<swapInThreshold&&!machine.suspendQueue.isEmpty()) {
			return swapIn(machine,currentMem);
		}
		return false;
	}
	private static boolean canSwap(Machine machine) {
		return machine.readyQueue().size>1||machine.cpuBusy()&&!machine.readyQueue().isEmpty();//if cpu is idle and theres only one process in ready queue that triggers 90% threshold, dont swap it out cuz cpu is about to work on said process, cuz what will end up happening is process gets swapped out, <70% threshold works, and swaps in infinitely
	}
	private static boolean swapOut(Machine machine,double currentMem) {
		PCB largest= getLargest(machine);
		machine.readyQueue().remove(largest);
	    machine.setUsedMemory(machine.usedMemory()-largest.memory());
		largest.setState(SUSPENDED);
		suspendQueue().addlast(largest);
		System.out.println("[Time " + machine.time() + " ms]");
		System.out.println("Medium-Term Scheduler Intervention");
		System.out.printf("Memory utilization: %.0f%%%n", currentMem);
		System.out.println("Action: Swap out Process " + largest.getPid());
		System.out.println("Process " + largest.getPid() + ": Ready -> Suspended");
		System.out.printf("Memory utilization after intervention: %.0f%%%n%n", utilization(machine));
		return true;
	}
	private static double utilization(Machine machine) {
		return (100.0*machine.usedMemory())/machine.Memory_MB);
	}
	private static boolean swapIn(Machine machine,double currentMem) {
		PCB firstFit=null;
		for(PCB pcb :machine.suspendQueue()) {
			if((100.0*(machine.usedMemory()+pcb.Memory()))/machine.Memory_MB<=90.0) {
				firstFit=pcb;
				break;
			}
		}
		if(firstFit==null)
			return false;
		
		machine.suspendQueue().remove(firstFit);
		machine.setUsedMemory(machine.usedMemory()+firstFit.Memory());
		machine.readyQueue().add(firstFit);
		firstFit.setState(READY);
		System.out.println("[Time " + machine.time() + " ms]");
		System.out.println("Medium-Term Scheduler Intervention");
		System.out.printf("Memory utilization: %.0f%%%n", currentMem);
		System.out.println("Action: Swap in Process " + firstFit.getPid());
		System.out.println("Process " + firstFit.getPid() + ":Suspended-> Ready ");
		System.out.printf("Memory utilization after intervention: %.0f%%%n%n", utilization(machine));
		return true;
	}
	private static PCB getLargest(Machine machine) {
		PCB p=null;
		for(PCB pcb :machine.readyQueue()) {
			if(p==null||pcb.Memory()>p.Memory()) {
				p=pcb;
			}
		}
		return p;
	}
}
