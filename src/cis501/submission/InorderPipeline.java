package cis501.submission;

/*TODO
 * skeleton code for additional tests
 * map out exactly how to do the bypass functions 
 * run sketch files 
 * incorporate the memory latency
 * for memory latency wrap in while loop and add stalls until it is not clogged
 */

import cis501.*;
import javafx.scene.effect.Light.Spot;

import java.util.LinkedList;
import java.util.Set;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

/**
 * Note: Stages are declared in "reverse" order to simplify iterating over them
 * in reverse order, as the simulator does.
 */
enum Stage {
	FETCH(0), DECODE(1), EXECUTE(2), MEMORY(3), WRITEBACK(4);

	private static Stage[] vals = values();
	private final int index;

	Stage(int idx) {
		this.index = idx;
	}

	/** Returns the index of this stage within the pipeline */
	public int i() {
		return index;
	}

	/** Returns the next stage in the pipeline, e.g., next after Fetch is Decode */
	public Stage next() {
		return vals[(this.ordinal() - 1 + vals.length) % vals.length];
	}
}

public class InorderPipeline implements IInorderPipeline {

	// private IBranchTargetBuffer btb;
	// private IDirectionPredictor bimodal;
	// private IDirectionPredictor gshare;
	// private IDirectionPredictor tournament;

	// bimodal = new DirPredBimodal(3/*index bits*/);

	// added to for branch prediction
	// InorderPipeline predict = new InorderPipeline(1,new BranchPredictor(never,
	// bimodal));
	// BranchPredictor predictor = new BranchPredictor();

	private BranchPredictor predict_type;
	private Direction predict_dir;
	private int latency;
	private Set<Bypass> bypasses;
	public Insn[] mylist = new Insn[7];
	private long[] expected_pc = new long[7];

	private static final int out = 0;
	private static final int fetch = 1;
	private static final int decode = 2;
	private static final int execute = 3;
	private static final int memory = 4;
	private static final int write = 5;
	private static final int stall_op = 6;
	private static final int FULL = 1;
	private static final int EMPTY = 0;

	private long mispredict_count = 0;

	private boolean first_instruction = true;
	private Direction predicted_direction;
	public int latency_count = 0;
	public int loaduse = 0;
	public int insn_count = 0;
	public int cycle_count = 0;

	private boolean stall_flag = false;
	private boolean mx_bypass;
	private boolean wx_bypass;
	private boolean wm_bypass;

	// this is if our array is empty or full
	private int fetch_state = 0;
	private int decode_state = 0;
	private int execute_state = 0;
	private int memory_state = 0;
	private int write_state = 0;
	private int out_state = 0;

	// BranchPredictor predict = new InorderPipeline(1,Bypass.FULL_BYPASS);
	// BranchPredictor brp = new BranchPredictor();

	
	private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
		return new Insn(dst, src1, src2, 1, 4, null, 0, null, mop, 1, 1, "stall");
	}
	
	/**
	 * 
	 * Create a new pipeline with the given additional memory latency.
	 *
	 * @param additionalMemLatency
	 *            The number of extra cycles mem insns require in the M stage. If 0,
	 *            mem insns require just 1 cycle in the M stage, like all other
	 *            insns. If x, mem insns require 1+x cycles in the M stage.
	 * @param bypasses
	 *            Which bypasses should be modeled. For example, if this is an empty
	 *            set, then your pipeline should model no bypassing, using stalling
	 *            to resolve all data hazards.
	 */
	public InorderPipeline(int additionalMemLatency, /* Set<Bypass> bypasses */ BranchPredictor bp) {
		this.latency = additionalMemLatency;
		// this.bypasses = bypasses;// because we are not given bypasses
		this.predict_type = bp; // set to what type we are using

	}

	@Override
	public String[] groupMembers() {
		return new String[] { "Eric Micallef", "Luis Garcia" };
	}

	// = normal 1 = mems take two clocks 2 = mems take three clocks ...etc

	@Override
	public void run(Iterable<Insn> uiter) {
		insn_count = 0;
		cycle_count = 0;

		// if we are sent Mx.Bypass we can only use mx bypass wx bypass is disabled
		// for testing purposes we will turn both of these flags on
		short stall = -1;
		boolean latency_load = false;
		first_instruction = true;

		mylist[stall_op] = makeInsn(stall, stall, stall, null);
		mylist[fetch] = makeInsn(stall, stall, stall, null);
		mylist[write] = makeInsn(stall, stall, stall, null);
		mylist[memory] = makeInsn(stall, stall, stall, null);
		mylist[decode] = makeInsn(stall, stall, stall, null);
		mylist[out] = makeInsn(stall, stall, stall, null);
		mylist[execute] = makeInsn(stall, stall, stall, null);
		// added so we could see more of the pipeline
		// create our stall instruction;

		for (Insn insn : uiter) {
			latency_load = false;
			insn_count++;

			//if (insn_count == 3220) {
				//System.out.println("stopping here for debugging purposes");
			//}

			if (latency != 0 && (insn.mem == MemoryOp.Load) || (insn.mem == MemoryOp.Store)) {
				cycle_count += latency;
				latency_count++;
			}

			// for a werid latency load use like line 52 53
			if (latency != 0 && (insn.srcReg1 == mylist[decode].dstReg || insn.srcReg2 == mylist[decode].dstReg)
					&& mylist[decode].mem == MemoryOp.Load && mylist[fetch].mem != MemoryOp.Load && wx_bypass == true) {
				// System.err.println("latency load use line " + MyData.insn_count);
				stall_flag = true;
				latency_load = true;
				// System.out.println(" load use line " + cycle_count + " " + insn );
				// because there are so many latencies the penalty is not added... line 560
				if (mylist[execute].mem == MemoryOp.Load || mylist[execute].mem == MemoryOp.Store)
					stall_flag = false;

				if (stall_flag == true) {
					next_cycle(mylist[stall_op]);
					stall_flag = false;
				}
			}

			if ((((insn.srcReg1 == mylist[fetch].dstReg || insn.srcReg2 == mylist[fetch].dstReg)
					&& mylist[fetch].mem == MemoryOp.Load)
					|| (insn.srcReg1 == mylist[decode].dstReg && insn.condCode == CondCodes.WriteCC
							&& mylist[decode].mem == MemoryOp.Load && latency != 0))
					&& (insn.mem != MemoryOp.Store && mylist[fetch].dstReg != -1)
					|| (insn.srcReg2 == mylist[fetch].dstReg && insn.mem == MemoryOp.Store
							&& mylist[fetch].mem == MemoryOp.Load)) {

				stall_flag = true;
				// why doe?
				if (insn.condCode == CondCodes.WriteCC && insn.srcReg1 == mylist[decode].dstReg
						&& mylist[decode].mem == MemoryOp.Load && latency != 0 && insn.srcReg2 == -1)
					stall_flag = false;

				// System.out.println("load use line number" + MyData.insn_count + " " +
				// insn.asm);
				// load use stall
				if (stall_flag == true) {
					loaduse++;
					next_cycle(mylist[stall_op]);
					stall_flag = false;
				}
			}

			next_cycle(insn);

			first_instruction = false;
		} // for insn

		for (int x = 0; x <= 4; x++) {
			next_cycle(mylist[stall_op]); // signifies just emptying the array
		} // while
		System.out.println("latency count " + latency_count);
		System.out.println("loaduse " + loaduse);
		System.out.println("insn_count: " + insn_count);
		System.out.println("mispredict " + mispredict_count);
		System.out.println("cycle_count: " + cycle_count);
	}// public void run ii

	@Override
	public long getInsns() {
		return insn_count;
	}

	@Override
	public long getCycles() {
		return cycle_count;
	}

	/*
	 * Function created by Eric Micallef with help from Luis Garcia Functionality is
	 * to shift instructions from one sequence to the next in the pipeline
	 * F->D->X->M->W
	 */
	public void next_cycle(Insn newinsn) {
		// we do not need to copy memory contents since this is last imp
		if (out_state == FULL) {
			out_state = EMPTY;
		}
		else {
			//do nothing or something
		}
		
		
		if (write_state == FULL) {
			mylist[out] = mylist[write];
			out_state = FULL; // set to full
			write_state = EMPTY; // set to empty we are shifting to the done stage
		} else {
			// our array is empty so we do nothing... or something?
		}

		// we need to copy and then set to empty
		if (memory_state == FULL) {
			
			mylist[write] = mylist[memory];
			write_state = FULL; // set to full
			memory_state = EMPTY; // set to empty we are shifting to the done stage
			
		} else {
			// our array is empty so we do nothing... or something?
		}

		// we need to copy and then set to empty
		if (execute_state == FULL) {
						
			mylist[memory] = mylist[execute];
			memory_state = FULL; // set to full
			execute_state = EMPTY; // set to empty we are shifting to the done stage
			
		} else {
			// our array is empty so we do nothing... or something?
		}

		// we need to copy and then set to empty
		if (decode_state == FULL) {
			
			mylist[execute] = mylist[decode];
			expected_pc[execute] = expected_pc[decode];
			execute_state = FULL;
			decode_state = EMPTY; // set to empty we are shifting to the done stage
			
			/* if we branched on a non branch dats bad */ 
			if(mylist[execute].branchType == null && mylist[execute].fallthroughPC() != expected_pc[execute] && mylist[execute].asm != "stall")
			{
				System.out.println("branched on non branch: " + mylist[execute].asm + " : " + insn_count);
				cycle_count+=2;
			}
			
			//in this stage of the pipeline we know whether or not its a branch
			if (mylist[execute].branchType != null && mylist[execute].asm != "stall")
			{
				// we did not take
				if(mylist[execute].fallthroughPC() == expected_pc[execute])
				{
					//we did not take and were wrong ): 
					if (mylist[execute].branchDirection != Direction.NotTaken)
					{
						predict_type.train(mylist[execute].pc, mylist[execute].branchTarget, mylist[execute].branchDirection);
						mispredict_count++;
						//System.out.println("not taken line: " + insn_count + " " + mylist[execute].asm);
						cycle_count +=2;
						
						
						if (latency != 0) {
							/* for the pop instructions which are loads and branches */
							if (mylist[execute].mem == MemoryOp.Load && mylist[execute].branchType != null)
							{
								//System.err.println("weird thing");
								cycle_count -=1;
							}
							/* for the latency */ 
							if (mylist[memory].mem == MemoryOp.Load || mylist[memory].mem == MemoryOp.Store) {
								cycle_count -= 1;
								//System.out.println("due to latency: " + mylist[execute].asm + " :" + insn_count );
							}
						} //if latency
					}//if we were wrong
					//else we did not take and were right (:
					else
					{
						predict_type.train(mylist[execute].pc, mylist[execute].fallthroughPC(), mylist[execute].branchDirection);
					}
				}//if not taken
				
				// we took
				/*else*/ if(mylist[execute].branchTarget == expected_pc[execute])
				{
					//we took and were wrong ):
					if (mylist[execute].branchDirection != Direction.Taken)
					{
						predict_type.train(mylist[execute].pc, mylist[execute].fallthroughPC(), mylist[execute].branchDirection);
						mispredict_count++;
						//System.err.println("taken line: " +  mylist[execute].asm);
						cycle_count +=2;
																		
						if (latency != 0) {
								
							/* for the pop instructions that are loads and branches */ 
							if (mylist[execute].mem == MemoryOp.Load && mylist[execute].branchType != null)
							{
								cycle_count -=1;
							}
							/* if we have a previous load or store due to latency */ 
							if (mylist[memory].mem == MemoryOp.Load || mylist[memory].mem == MemoryOp.Store) {
								cycle_count -= 1;
								//System.out.println("due to latency");
							}
						}//if latency
					}// if not taken
					//else we took and were right 
					else {
						predict_type.train(mylist[execute].pc, mylist[execute].branchTarget, mylist[execute].branchDirection);
					}
				}//else if 
				
			}// if was a branch 
		} //if state == full 
		
		// we need to copy and then set to empty
		if (fetch_state == FULL) {
			mylist[decode] = mylist[fetch];
			expected_pc[decode] = expected_pc[fetch];
			decode_state = FULL; // set to full
			fetch_state = EMPTY; // set to empty we are shifting to the done stage
		}
		// since the fetch is empty this is where we begin unless it is the end ofthe
		// file in that case we have no new data and need to close it off
		if (fetch_state == EMPTY) {
			// stall means its a fake instruction
			if(newinsn.asm != "stall")
				expected_pc[fetch] = predict_type.predict(newinsn.pc, newinsn.fallthroughPC());
			// copy the data we received and mark as full;
			mylist[fetch] = newinsn;
			cycle_count++;
			fetch_state = FULL;
		}
	}// end of next_cycle()

}// main