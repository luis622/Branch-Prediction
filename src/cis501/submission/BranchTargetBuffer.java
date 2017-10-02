package cis501.submission;
import java.util.*;  

import cis501.IBranchTargetBuffer;


public class BranchTargetBuffer implements IBranchTargetBuffer {
	
	HashMap<Long,Long> hm = new HashMap<Long, Long>();
	private long[][] BTB = new long[20][3];
	private long prediction;
	private long confirmed_pred;
	
	private final int IDX = 0;
	private final int TAG = 1;
	private final int TARGET = 2;
	
	private long mask = 0;
	
    public BranchTargetBuffer(int indexBits) {

    	for (int i = 0; i < indexBits; i++)
    		mask |= (1 << i);
    	
    	//mask &= ~(1);
    }

    
    
    @Override
    public long predict(long pc) {
    	
    	long index = pc & mask;
    	// System.out.println("hash " + hm.get(index));
    	// long rval = (hm.get(index) == null) ? 0 : hm.get(index);
    	// return rval;
    	// 2d array [0] = index [1] = tag [2] = target
    	//add these defines in later
    	prediction = BTB[(int) index][TAG];
    	//if they are equal then keep prediction else return 0
    	prediction = (prediction == pc) ? BTB[(int) index][TARGET] : 0;
    	 // 8 =     00001000 pc
    	// 0        00000000
    	//mmask     00011100
    	// 100 =    01100100 predict
    	//index is 3 = 11100
    	// 42 =     00101010
    	return prediction;
    }

    @Override
    public void train(long pc, long actual) {

    	long index = pc & mask;
    	//hm.put(index,actual);
    	BTB[(int) index][TAG] = pc;
    	BTB[(int) index][TARGET] = actual;
    	
    }
}
