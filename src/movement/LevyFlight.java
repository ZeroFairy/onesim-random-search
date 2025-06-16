package movement;

import core.Coord;
import core.Settings;

/**
 * Generates movement paths according to human-like behavior,
 * where short steps are more frequent than long ones (Lévy Flight pattern).
 *
 * @see <a href="https://en.wikipedia.org/wiki/L%C3%A9vy_flight">Lévy flight</a>
 * @see <a href="https://ieeexplore.ieee.org/document/5750071">On the Levy-Walk Nature of Human Mobility</a>
 */
public class LevyFlight extends MovementModel {
	/** Number of waypoints per path */
	private static final int PATH_LENGTH = 1;
	private Coord lastWaypoint;

	/** Controls how heavy the distribution tail is */
	public static final String ALPHA = "alpha";
	public static final double DEFAULT_ALPHA = 0.5;

	public static final String XM = "xm";
	public static final double DEFAULT_XM = 1;

	private double xm, alpha;

	public LevyFlight(Settings settings) {
		super(settings);
		this.alpha = settings.contains(ALPHA) ? settings.getDouble(ALPHA) : DEFAULT_ALPHA;
		this.xm = settings.contains(XM) ? settings.getDouble(XM) : DEFAULT_XM;
	}

	protected LevyFlight(LevyFlight rwp) {
		super(rwp);
		this.xm = rwp.xm;
		this.alpha = rwp.alpha;
	}

	/**
	 * Returns a random initial location for a host on the map.
	 * @return Random position on the map
	 */
	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		Coord c = randomCoord();
		this.lastWaypoint = c;
		return c;
	}

	@Override
	public Path getPath() {
		Path p = new Path(generateSpeed());
		p.addWaypoint(lastWaypoint.clone());

		Coord c = levyFlight();
		p.addWaypoint(c);

		this.lastWaypoint = c;
		return p;
	}

	@Override
	public LevyFlight replicate() {
		return new LevyFlight(this);
	}

	/**
	 * Generates a random coordinate within the bounds of the map.
	 */
	protected Coord randomCoord() {
		return new Coord(
				rng.nextDouble() * getMaxX(),
				rng.nextDouble() * getMaxY()
		);
	}

	/**
	 * Performs a Lévy Flight step to determine the next coordinate.
	 */
	protected Coord levyFlight() {
		double next_X, next_Y;

		do {
			double step_length = pareto();
			double theta = rng.nextDouble(0, 2 * Math.PI);

			next_X = lastWaypoint.getX() + step_length * Math.cos(theta);
			next_Y = lastWaypoint.getY() + step_length * Math.sin(theta);

		} while (next_X >= getMaxX() || next_Y >= getMaxY() || next_X <= 0 || next_Y <= 0);

		return new Coord(next_X, next_Y);
	}

	/**
	 * Calculates a step length based on the Pareto distribution.
	 * @return A value representing the step length
	 */
	public double pareto() {
		double u = 1 - rng.nextDouble(); // Ensures u is in (0, 1]
		return this.xm / Math.pow(u, 1 / this.alpha);
	}
}
