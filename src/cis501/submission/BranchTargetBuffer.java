package cis501.submission;
import java.util.*;  

import cis501.IBranchTargetBuffer;


public class BranchTargetBuffer implements IBranchTargetBuffer {
	
	//HashMap<Long,Long> hm = new HashMap<Long, Long>();
	public long[][] BTB;
	public long prediction;
	public long tag;
	//private long confirmed_pred;
	public long indexBTB = 0;
	private final int IDX = 0;
	private final int TAG = 1;
	private final int TARGET = 2;
	private long mask = 0;
	
    public BranchTargetBuffer(int indexBits) {

    	this.BTB = new long[(int) Math.pow(2, indexBits)][3];
    	this.indexBTB = indexBits;
    	//for (int i = 0; i < indexBits; i++)
    		//mask |= (1 << i);
    	    	
    	mask = (int) (Math.pow(2, indexBits) -1);
    	//System.out.println("index bits " + indexBits);
    	//System.out.println("mask " + mask);
    	 //mask &= ~(1);
    }

    
    
    @Override
    public long predict(long pc) {
    	
    	indexBTB = pc & mask;
    	// 2d array [0] = index [1] = tag [2] = target
    	//we need to predict if its a branch AND its next target
    	//tag = hm.get(index);
    	//this.tag = (int) BimodalBHT[(int) indexBTB][1];
    	tag = BTB[(int) indexBTB][TAG]; //gets target  
    	
    	//is there something in the table?
    	//if yes then get the target
    	//if they are equal then keep prediction else return pc
    	prediction = (tag == pc) ? BTB[(int) indexBTB][TARGET] : 0;
    	//System.out.println(prediction);
    	return prediction;
    }

    @Override
    public void train(long pc, long actual) {

    	long index = pc & mask;

    	BTB[(int) index][TAG] = pc;
    	BTB[(int) index][TARGET] = actual;
    	
    }
}
