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
    //private IDirectionPredictor gshare;
    //private IDirectionPredictor tournament;
    
   // bimodal = new DirPredBimodal(3/*index bits*/);
	
	// added to for branch prediction
	//InorderPipeline predict = new InorderPipeline(1,new BranchPredictor(never, bimodal));
	//BranchPredictor predictor = new BranchPredictor();
	
	private BranchPredictor predict_type;
	private int latency;
	private Set<Bypass> bypasses;
	public Insn[] mylist = new Insn[7];
	//private static final int new_insn = 0;
	private static final int fetch = 1;
	private static final int decode = 2;
	private static final int execute = 3;
	private static final int memory = 4;
	private static final int write = 5;
	private static final int stall_op = 6;
	private static final int FULL = 1;
	private static final int EMPTY = 0;
	
	private long expected_pc = 0;
	
	private long WX_count;
	private long WM_count; 
	private long MX_count;
	
	private boolean first_instruction = true;
	private Direction predicted_direction;
	public int latency_count = 0;
	public int loaduse = 0;
	public int insn_count = 0;
	public int cycle_count = 0;
	
	private boolean stall_flag = false;
	private  boolean mx_bypass;
	private boolean wx_bypass;
	private boolean wm_bypass;
	
	//this is if our array is empty or full
	private int fetch_state = 0;
	private int decode_state = 0;
	private int execute_state = 0;
	private int memory_state = 0;
	private int write_state = 0;
	
	//BranchPredictor predict = new InorderPipeline(1,Bypass.FULL_BYPASS);
	//BranchPredictor brp = new BranchPredictor();
	
	
    private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
        return new Insn(dst, src1, src2,
                1, 4,
                null, 0, null,
                mop, 1, 1,
                "synthetic");
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
	public InorderPipeline(int additionalMemLatency, /*Set<Bypass> bypasses*/ BranchPredictor bp) {
		this.latency = additionalMemLatency;
		//this.bypasses = bypasses;// because we are not given bypasses
		this.predict_type = bp; //set to what type we are using
		
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
		// bools for determing hazards 
		boolean check_WX = true;
		boolean check_MX = false;
		boolean check_WM = false;
		
		mylist[stall_op] = makeInsn(stall, stall, stall, null); 
		mylist[fetch] = makeInsn(stall, stall, stall, null); 
		mylist[write] = makeInsn(stall, stall, stall, null); 
		mylist[memory] = makeInsn(stall, stall, stall, null); 
		mylist[decode] = makeInsn(stall, stall, stall, null); 
		
		// create our stall instruction;
		
		mx_bypass = false; // true means we can not use the bypass false means we can
		wx_bypass = false;
		wm_bypass = false; //false meand we can use for Branchprediction we are modeling fullbypass

		// since we are no longer being passed a bypass... for this assignment we are modeling full
		/*if (bypasses.contains(Bypass.MX))
			mx_bypass = false;
		if (bypasses.contains(Bypass.WX))
			wx_bypass = false;
		if (bypasses.contains(Bypass.WM))
			wm_bypass = false;
*/
		for (Insn insn : uiter) {
			latency_load = false;
			insn_count++;
			
		//	if (MyData.insn_count == 470) {
		//		System.out.println("stopping here for debugging purposes");
		//	}

			if ((insn.mem == MemoryOp.Load) || (insn.mem == MemoryOp.Store)) {
				cycle_count += latency;
				latency_count++;
			}
			
			// for a werid latency load use like line 52 53
			if (latency != 0 && (insn.srcReg1 == mylist[decode].dstReg || insn.srcReg2 == mylist[decode].dstReg) && mylist[decode].mem == MemoryOp.Load && mylist[fetch].mem != MemoryOp.Load && wx_bypass == true)
				{
				//	System.err.println("latency load use line " + MyData.insn_count);
					stall_flag = true;
					latency_load = true;
					//System.out.println(" load use line " + cycle_count + " " + insn );
					// because there are so many latencies the penalty is not added... line 560
					if (mylist[execute].mem == MemoryOp.Load || mylist[execute].mem == MemoryOp.Store)
						stall_flag = false;
					
					if (stall_flag == true) {
						//next_cycle(stall, stall, stall, dummy, dummy);
						next_cycle(mylist[stall_op]);
						// MyData.cycle_count++;
						stall_flag = false;
					}
				}
			
			if ((((insn.srcReg1 == mylist[fetch].dstReg || insn.srcReg2 == mylist[fetch].dstReg)
			&& mylist[fetch].mem == MemoryOp.Load)
			|| (insn.srcReg1 == mylist[decode].dstReg && insn.condCode == CondCodes.WriteCC
					&& mylist[decode].mem == MemoryOp.Load && latency != 0))
			&& (insn.mem != MemoryOp.Store && mylist[fetch].dstReg != -1)
	|| (insn.srcReg2 == mylist[fetch].dstReg && insn.mem == MemoryOp.Store && mylist[fetch].mem == MemoryOp.Load)){

				stall_flag = true;
				// why doe?
				if (insn.condCode == CondCodes.WriteCC && insn.srcReg1 == mylist[decode].dstReg
						&& mylist[decode].mem == MemoryOp.Load && latency != 0 && insn.srcReg2 == -1)
					stall_flag = false;
				
				//System.out.println("load use line number" + MyData.insn_count + " " + insn.asm);
				loaduse++;
				// load use stall
				if (stall_flag == true) {
					next_cycle(mylist[stall_op]);
					// MyData.cycle_count++;
					stall_flag = false;
				}
			}

			check_MX = true; // set to enter bypass loop

			while (check_MX == true || check_WX == true || check_WM == true) {
				check_MX = MX_bypass(insn);
				if (stall_flag == true) {
					next_cycle(mylist[stall_op]);
					stall_flag = false;
				}
				check_WX = WX_bypass(insn);
				if (stall_flag == true) {
					next_cycle(mylist[stall_op]);
					stall_flag = false;
				}
				check_WM = WM_bypass(insn);
				if (stall_flag == true) {
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
		System.out.println("WX count " + WX_count);
		System.out.println("MX count " + MX_count);
		System.out.println("WM count " + WM_count);
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
		if (write_state == FULL) {
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
			// train our predictor if it needs to be trained also ensure no stalls make it here
			//if(mylist[execute].branchDirection == predicted_direction && mylist[execute].pc != 1)
			//{
				//predict_type.train(mylist[execute].pc, mylist[execute].pc, mylist[execute].branchDirection);
			//}
			mylist[memory] = mylist[execute];
			memory_state = FULL; // set to full
			execute_state = EMPTY; // set to empty we are shifting to the done stage
			
		} else {
			// our array is empty so we do nothing... or something?
		}

		// we need to copy and then set to empty
		if (decode_state == FULL) {
			mylist[execute] = mylist[decode];
		//	Mylist.predictedpc[execute] = Mylist.predictedpc[decode];
			execute_state = FULL;
			decode_state = EMPTY; // set to empty we are shifting to the done stage
		} else {
			// our array is empty so we do nothing... or something?
		}

		// we need to copy and then set to empty
		if (fetch_state == FULL) {
			mylist[decode] = mylist[fetch];
			//Mylist.predictedpc[decode] = Mylist.predictedpc[fetch];
			decode_state = FULL; // set to full
			fetch_state = EMPTY; // set to empty we are shifting to the done stage
		}
		// since the fetch is empty this is where we begin unless it is the end ofthe
		// file in that case we have no new data and need to close it off
		if (fetch_state == EMPTY) {
			//ARM
			// if it is equal to 1 that means we manually put a stall in
			//if(newinsn.pc != 1)
				//expected_pc = predict_type.predict(newinsn.pc, newinsn.fallthroughPC());
						
			// copy the data we received and mark as full;
			//BranchPredictor predictor = new BranchPredictor.predict(newinsn.pc, newinsn.fallthroughPC());
			mylist[fetch] = newinsn;
			//Mylist.predictedpc[fetch] = expected_pc;
			cycle_count++;
			fetch_state = FULL;
		}
	}// end of next_cycle()

	public boolean WX_bypass(Insn newinsn) {
		// function for determing if we need a bypass
		// we need WX bypassing when the Write section has a register value of
		// importance to the X sec
		// ie X: add r4<-r3,r2 W: add r3<-r2,r1
		// look at the sources of X and destination of W
		if ((mylist[decode].dstReg == newinsn.srcReg1) 
				|| (mylist[decode].dstReg == newinsn.srcReg2)
				&& first_instruction == false)	
		{	
			// checking for if it is read or write immediate 
			if(newinsn.mem == null && mylist[decode].mem != MemoryOp.Load)
				if (newinsn.condCode != CondCodes.WriteCC && (mylist[decode].condCode == CondCodes.ReadCC || mylist[decode].condCode == CondCodes.ReadWriteCC ))
					return false;
			
			//if (MyData.latency_load == true)
				//return false;
			
			//for back to back loads
			//if (mem == Operation.OTHER && MyData.decode[Operation.MEMOP] == Operation.LOAD && MyData.fetch[Operation.MEMOP] == Operation.LOAD)
				//return false;
			
			//check to see if you could have used an MX BYPASS
			if((mylist[fetch].dstReg == newinsn.srcReg1 || mylist[fetch].dstReg == newinsn.srcReg2) && (mylist[decode].dstReg == mylist[fetch].dstReg) && mx_bypass== false)
				return false;
			//check to see if we could have used a WM BYPASS
			if((mylist[fetch].dstReg == newinsn.srcReg1) && mylist[fetch].mem == MemoryOp.Load && newinsn.mem == MemoryOp.Store && wm_bypass == false)
				return false;
			//if this occurs then we no stall because the latency does this for us.
			if(latency != 0 && (mylist[fetch].mem == MemoryOp.Load || mylist[fetch].mem == MemoryOp.Store) && (mylist[decode].mem == null || mylist[decode].mem == MemoryOp.Load)) 
				return false;
			 		
			 if (mylist[decode].dstReg == -1)
				return false;
			// we need a bypass
			if (wx_bypass == true)// this flag will be set if we can not use this type of bypass
			{
				// we need a stall if we are here
				stall_flag = true;
				WX_count++;
				// System.out.println(reg1 + " " + reg2 + " " + " "+
				return true;
			}
		}
		return false;
	}

	public boolean MX_bypass(Insn newinsn) {
		// function for determing if we need a MX bypass
		// we need MX bypassing when the memory section has a register value of
		// importance to the X sec
		// ie X: add r4<-r3,r2 M: add r3<-r2,r1
		// look at the sources of X and destination of W
		if ((mylist[fetch].dstReg == newinsn.srcReg1) 
				|| (mylist[fetch].dstReg == newinsn.srcReg2)
				&& first_instruction == false && mylist[fetch].mem != MemoryOp.Load)
		{			
			 if (mylist[fetch].dstReg == -1)
				 return false;
			 
			// we need a bypass
			if (mx_bypass == true)// this flag will be set if we can not use this type of bypass
			{
				// we need a stall if we are here
				stall_flag = true;
				MX_count++;
				// System.out.println(reg1 + " " + reg2 + " " + mem + " "+)
				return true;
			} // if flag
		} // if giant condition
		return false;
	}// mx bypass

	public boolean WM_bypass(Insn newinsn) {
		// function for determing if we need a MX bypass
		// we need MX bypassing when the memory section has a register value of
		// importance to the X sec
		// ie X: add r4<-r3,r2 M: add r3<-r2,r1
		// look at the sources of X and destination of W
		// if((MyData.fetch[Operation.DEST] == dest) && MyData.first_instruction ==
		// false && dest != -1 && mem == Operation.STORE &&
		// MyData.fetch[Operation.MEMOP] == Operation.LOAD )
		if ((mylist[fetch].dstReg == newinsn.srcReg1) && first_instruction == false
				&& mylist[fetch].dstReg != -1 && newinsn.mem != MemoryOp.Load /*could also be != load or == store*/
				&& mylist[fetch].mem == MemoryOp.Load) {
			// we need a bypass
			if (wm_bypass == true)// this flag will be set if we can not use this type of bypass
			{
				
				//check to see if you could have used an MX BYPASS
				if((mylist[fetch].dstReg == newinsn.srcReg1 || mylist[fetch].dstReg == newinsn.srcReg2) && (mylist[decode].dstReg == mylist[fetch].dstReg) && mx_bypass == false)
						return false;
				
				// we need a stall if we are here
				stall_flag = true;
				WM_count++;
				//System.out.println("WM line stall" + MyData.insn_count);
				return true;
			} // if flag
		} // if giant condition
		return false;
	}// WM bypass	
}// main
