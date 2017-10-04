package cis501.submission;

import cis501.Direction;

public class DirPredGshare extends DirPredBimodal {

	public long[][] GShareBHT;
	public int prediction = 0;
	public int confirmPred = 0;
	public int BHR = 0;
	public int mask = 0;
	public long indexBHT = 0;
	public long historyBits = 0;

	public long[][] BHT;

	public DirPredGshare(int indexBits, int historyBits) {
		super(indexBits);

		this.GShareBHT = new long[(int) Math.pow(2, indexBits)][3];

		this.historyBits = historyBits;
		
		//BHR = historyBits;

		mask = (int) (Math.pow(2, indexBits) - 1);
	}

	@Override
	public Direction predict(long pc) {

		// XOR to to generate index to BHT
		indexBHT = BHR ^ pc;

		// MASK to clear bits not needed
		indexBHT = indexBHT & mask;

		prediction = (int) GShareBHT[(int) indexBHT][1];

		/**
		 * prediction <= 1: Not Taken 
		 * prediction >= 2: Taken
		 */
		
		switch (prediction) {
		case 0:
		case 1: {
			return Direction.NotTaken;
		}
		case 2:
		case 3: {
			return Direction.Taken;
		}
		default: {
			System.out.println("Error on GShare prediction.");
			return Direction.Taken;
		}
		}
	}

	@Override
	public void train(long pc, Direction actual) {

		// hash PC with Branch History Register, to the the BHT index
		indexBHT = BHR ^ pc;

		// mask our index with number of index bits desired
		indexBHT = indexBHT & mask;

		confirmPred = (int) GShareBHT[(int) indexBHT][1];

		// Train branch predictor
		if (Direction.Taken == actual) {
			// Update BHR with taken
			BHR = (BHR << 1);
			BHR |= 1;

			// Update if taken and current counter is (NT, nt or t)
			// Saturate the counter if current state is '3'
			if (confirmPred < 3) {
				GShareBHT[(int) indexBHT][1] += 1;
			}

			// Update BTB table only if taken
		}
		if (Direction.NotTaken == actual) {
			// Update BHR with not taken
			BHR = (BHR << 1);
			BHR |= 0;
			
			// Update if not taken and current counter is (nt, t or T)
			// Saturate the counter if current state is '0'
			if (confirmPred > 0) {
				GShareBHT[(int) indexBHT][1] -= 1;
			}
		}

		// Clearing the nth bit (to model a bounded-size history)
		BHR &= ~(1 << historyBits);
	}
}