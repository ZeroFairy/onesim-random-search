package movement;

import core.Coord;
import core.Settings;

/**
 * Pure generated stationary abstract class. Differs from {@link StationaryMovement}.
 *
 * @author narwa
 */
public abstract class StationaryNodes extends MovementModel {

	protected Coord location;

	public StationaryNodes(Settings s) {
		super(s);
	}

	public StationaryNodes(StationaryNodes sp) {
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
		return new Coord(rng.nextDouble() * getMaxX(), rng.nextDouble() * getMaxY());
	}

	/**
	 * Returns a single coordinate path (using the only possible coordinate)
	 *
	 * @return a single coordinate path
	 */
	@Override
	public Path getPath() {
		Path p = new Path(0);
		p.addWaypoint(location);
		return p;
	}

	@Override
	public double nextPathAvailable() {
		return Double.MAX_VALUE;    // no new paths available
	}

	public abstract StationaryNodes replicate();
}
