package cis501.submission;

import cis501.Direction;

public class DirPredGshare extends DirPredBimodal {

	private long[][] GShareBHT;
	private int prediction = 0;
	private int confirmPred = 0;
	private int BHR = 0;
	private int maskIndex = 0;
	private long indexBHT = 0;

	public long[][] BHT;
	public long indexBits = 0;

	public DirPredGshare(int indexBits, int historyBits) {
		super(indexBits);

		this.GShareBHT = new long[(int) Math.pow(2, indexBits)][3];
		this.indexBits = indexBits;
		
		
		//indexPC = indexBits;
		this.BHR = historyBits;

		//'inderxBits + 1': In BranchPredictor PC is already shifted once 
		for (int i = 0; i < indexBits + 1; i++) {
			maskIndex |= (1 << i);
		}
		//Clears the least significant bit by anding with: 0000 0001 => 1111 1110 
		maskIndex &= ~(1);
	}

	@Override
	public Direction predict(long pc) {

		//XOR to to generate index to BHT
		indexBHT = BHR ^ pc;

		//MASK to clear bits not needed
		indexBHT = indexBHT & maskIndex;
		
		prediction = (int) GShareBHT[(int) indexBHT][1];

		/**
		 * prediction <= 1: Not Taken
		 * prediction >= 2: Taken
		 */
		switch (prediction) {
		case 0:
		case 1:
			return Direction.NotTaken;
		case 2:
		case 3:
			return Direction.Taken;
		default: {
			System.out.println("Error on GShare prediction.");
			return Direction.Taken;
		}
		}
	}

	@Override
	public void train(long pc, Direction actual) {
		
		indexBHT = BHR ^ pc;
		
		indexBHT = indexBHT & maskIndex;

		//Left shift once for new history
		//BHR |= (1 << 1);
		//Clearing the nth bit (to model a bounded-size history)
		//BHR &= ~(1 << indexPC);
		
		confirmPred = (int) GShareBHT[(int) indexBHT][1];

		if (Direction.Taken == actual) {
			//Update BHR with taken
			BHR |= (1 << 0);
			
			//Update if taken and current counter is (NT, nt or t)
			if (confirmPred < 3) {
				GShareBHT[(int) indexBHT][1] += 1;
			}
			
			//Update BTB table only if taken
		}
		if (Direction.NotTaken == actual) {
			//Update BHR with not taken
			BHR &= ~(1 << 0);
			
			//Update if not taken and current counter is (nt, t or T)
			if (confirmPred > 0) {
				GShareBHT[(int) indexBHT][1] -= 1;
			}
		}
		
		//Clearing the nth bit (to model a bounded-size history)
		BHR &= ~(1 << indexBits);
	}
}
