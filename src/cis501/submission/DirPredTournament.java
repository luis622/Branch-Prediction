package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;

public class DirPredTournament extends DirPredBimodal {

	public IDirectionPredictor localPredictor; // Bimodal Sub-predictor 'predictorNT'
	public IDirectionPredictor globalPredictor; // GShare Sub-predictor 'predictorT'

	public long[][] MetaBHT; // History table for Meta predictor

	public int localPrediction = 0; // NotTaken
	public int globalPrediction = 0; // Taken
	public int metaPredictor = 0;

	public int mask = 0;
	public long indexBits = 0; // Index for metaBHT

	public DirPredTournament(int chooserIndexBits, IDirectionPredictor predictorNT, IDirectionPredictor predictorT) {
		super(chooserIndexBits); // re-use DirPredBimodal as the chooser table

		this.MetaBHT = new long[(int) Math.pow(2, chooserIndexBits)][3];
		this.localPredictor = predictorNT; // Instantiate localPredictor' with Bimodal methods
		this.globalPredictor = predictorT; // Instantiate globalPredictor' with GShare methods

		indexBits = (int) (Math.pow(2, chooserIndexBits) - 1);
	}

	@Override
	public Direction predict(long pc) {

		indexBHT = pc & mask;
		metaPredictor = (int) MetaBHT[(int) indexBHT][1];

		switch (metaPredictor) {
		case 0:
		case 1: {
			return localPredictor.predict(pc); // Return prediction from GShare
		}
		case 2:
		case 3: {
			return globalPredictor.predict(pc); // Return prediction from Bimodal
		}
		default: {
			return Direction.Taken;
		}
		}
	}

	@Override
	public void train(long pc, Direction actual) {

		indexBHT = pc & mask;

		metaPredictor = (int) MetaBHT[(int) indexBHT][1];

		if ((localPredictor.predict(pc) == actual) && (globalPredictor.predict(pc) == actual)
				|| (localPredictor.predict(pc) != actual) && (globalPredictor.predict(pc) != actual)) {

		} else if ((localPredictor.predict(pc) == actual) && (globalPredictor.predict(pc) != actual)) {

			if (metaPredictor > 0) {
				MetaBHT[(int) indexBHT][1] -= 1;
			}
		} else if ((globalPredictor.predict(pc) == actual) && (localPredictor.predict(pc) != actual)) {
			if (metaPredictor < 3) {
				MetaBHT[(int) indexBHT][1] += 1;
			}
		} else {
		}

		localPredictor.train(pc, actual);
		globalPredictor.train(pc, actual);
	}
}
