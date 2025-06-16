package movement;

import core.Coord;
import core.Settings;

/**
 * Generated Gaussian stationary movement.
 *
 * @author narwa
 */
public class StationaryGaussian extends StationaryNodes {

	private Coord location;

	public StationaryGaussian(Settings s) {
		super(s);
	}

	public StationaryGaussian(StationaryGaussian sp) {
		super(sp);
		this.location = sp.location;
	}

	/**
	 * Returns the only location of this movement model
	 *
	 * @return the only location of this movement model
	 */
	@Override
	public Coord getInitialLocation() {
		double x;
		double y;
		do {
			/* Adjusting standard deviation scaling factor */
			/* (Assuming 99.7% data within [-3*std, 3*std], empirical rule) */
			double stdDevFactorX = getMaxX() / 3.0;
			double stdDevFactorY = getMaxY() / 3.0;

			x = rng.nextGaussian() * stdDevFactorX + getMaxX() / 2.0;
			y = rng.nextGaussian() * stdDevFactorY + getMaxY() / 2.0;

		} while (x > getMaxX() || y > getMaxY() || x < 0 || y < 0);
		return new Coord(x, y);
	}

	@Override
	public StationaryGaussian replicate() {
		return new StationaryGaussian(this);
	}
}
