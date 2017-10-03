package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;

public class DirPredBimodal implements IDirectionPredictor {

	//private long[][] BimodalBHTBHT = new long[20][3];
	private int prediction;
	private long confirmPred;
	public long[][] BimodalBHT;
	public long indexBits = 0;
	public int mask = 0;

	public DirPredBimodal(int indexBits) {

		this.BimodalBHT = new long[(int) Math.pow(2, indexBits)][3];
		this.indexBits = indexBits;

		// 'inderxBits + 1': In BranchPredictor PC is already shifted once
		//eric took off 
		for (int i = 0; i < indexBits; i++) {
			this.mask |= (1 << i);
		}

		//Clears the least significant bit by anding with: 0000 0001 => 1111 1110
		//mask &= ~(1);
				
		/*
		// Clears the least significant bit by anding with: 0000 0001 => 1111 1110
		this.mask &= ~(1);

		//'inderxBits + 1': In BranchPredictor PC is already shifted once 
		for (int i = 0; i < indexBits + 1; i++) { 
			mask |= (1 << i);
		}
		  
		//Clears the least significant bit by anding with: 0000 0001 => 1111 1110
		mask &= ~(1);
		
		*/
		// Use a Try, catch incase indexBits is larger than 20
	}

	@Override
	public Direction predict(long pc) {

		this.indexBits = pc & this.mask;
		
		// prediction = (int) BimodalBHTBHT[(int) indexBits][1];
		prediction = (int) this.BimodalBHT[(int) this.indexBits][1];

		switch (prediction) {
		case 0:
		case 1:
			return Direction.NotTaken;
		case 2:
		case 3:
			return Direction.Taken;
		default: {
			System.out.println("Error on bimodal prediction.");
			return Direction.Taken;
		}
		}
	}

	@Override
	public void train(long pc, Direction actual) {
		
		this.indexBits = pc & this.mask;

		confirmPred = this.BimodalBHT[(int) indexBits][1];

		// Update if taken and current counter is (NT, nt or t)
		if (Direction.Taken == actual && (confirmPred < 3)) {
			// Update BimodalBHT to taken
			this.BimodalBHT[(int) indexBits][1] += 1;

			// Update BTB table only if taken
		}
		// Update if not taken and current counter is (nt, t or T)
		if (Direction.NotTaken == actual && (confirmPred > 0)) {
			// Update BimodalBHT to not taken
			this.BimodalBHT[(int) indexBits][1] -= 1;
		
		/*
		// confirmPred = (int) BimodalBHTBHT[(int) indexBits][1];
		long indexBits = pc & mask;

		// confirmPred = (int) BimodalBHTBHT[(int) indexBits][1];
		confirmPred = BimodalBHT[(int) pc];

		// Update if taken and current counter is (NT, nt or t)
		if (Direction.Taken == actual && (confirmPred < 3)) {
			// Update BimodalBHT to taken
			BimodalBHT[(int) indexBits][1] += 1;

			// Update BTB table only if taken
		}
		// Update if not taken and current counter is (nt, t or T)
		if (Direction.NotTaken == actual && (confirmPred > 0)) {
			// Update BimodalBHT to not taken
			BimodalBHT[(int) indexBits][1] -= 1;
		 */
		}
		//System.out.println("index: " + indexBits + " " + BimodalBHT[(int) indexBits][1]);
	}

}
