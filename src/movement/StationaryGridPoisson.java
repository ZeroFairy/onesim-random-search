package movement;

import core.Coord;
import core.Settings;

import java.util.Random;

/**
 * Generated Poisson stationary movement.
 *
 * @author narwa
 */
public class StationaryGridPoisson extends StationaryNodes {

	public StationaryGridPoisson(Settings s) {
		super(s);
	}

	public StationaryGridPoisson(StationaryGridPoisson sp) {
		super(sp);
	}

	/**
	 * Returns the only location of this movement model
	 *
	 * @return the only location of this movement model
	 */
	@Override
	public Coord getInitialLocation() {
		return new Coord(rng.nextGaussian() * getMaxX(), rng.nextGaussian() * getMaxY());
	}

	@Override
	public StationaryGridPoisson replicate() {
		return new StationaryGridPoisson(this);
	}

	static class GridPoissonGenerator {
		/**
		 * Knuth Algorithm - Inverse transform method
		 *
		 */
		public static int poissonRandom(double lambda, Random random) {
			double L = Math.exp(-lambda);
			double p = 1.0;
			int k = 0;

			do {
				k++;
				p *= random.nextDouble();
			} while (p > L);
			return k - 1;
		}
	}
}
