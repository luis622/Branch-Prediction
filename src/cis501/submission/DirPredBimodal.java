package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;

public class DirPredBimodal implements IDirectionPredictor {

	//private long[][] BHTBHT = new long[20][3];
	public int prediction;
	public long confirmPred;
	public long[][] BimodalBHT;
	public long indexBHT = 0;
	public int mask = 0;

	public DirPredBimodal(int indexBits) {

		this.BimodalBHT = new long[(int) Math.pow(2, indexBits)][3];
		this.indexBHT = indexBits;

		mask = (int) (Math.pow(2, indexBits) - 1);
	}

	@Override
	public Direction predict(long pc) {

		indexBHT = pc & mask;
		
		// this.prediction = (int) BHTBHT[(int) indexBHT][1];
		this.prediction = (int) BimodalBHT[(int) indexBHT][1];

		//Determine if N(strong not taken), n(weakly not taken), t(weak taken), T(Strong taken)
		switch (this.prediction) {
		case 0:
		case 1:{
			return Direction.NotTaken;
		}
		case 2:
		case 3:{
			return Direction.Taken;
		}
		default: {
			return Direction.Taken;
		}
		}
	}

	@Override
	public void train(long pc, Direction actual) {

		//mask our index with number of index bits desired
		indexBHT = pc & mask;

		confirmPred = BimodalBHT[(int) indexBHT][1];

		// Train branch predictor
		// Update if taken and current counter is (NT, nt or t)
		// Saturate the counter if current state is '3'
		if (Direction.Taken == actual && (confirmPred < 3)) {
			// Update BHT to taken
			BimodalBHT[(int) indexBHT][1] += 1;

			// Update BTB table only if taken
		}
		// Update if not taken and current counter is (nt, t or T)
		// Saturate the counter if current state is '0'
		if (Direction.NotTaken == actual && (confirmPred > 0)) {
			// Update BHT to not taken
			BimodalBHT[(int) indexBHT][1] -= 1;
		}

	}
}
