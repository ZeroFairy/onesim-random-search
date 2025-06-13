package movement;

import core.Coord;
import core.Settings;

/**
 * Create path that were according to a human movement.
 * Where there are more short length path, then long length path.
 */
public class LevyFlight extends MovementModel {
	/** how many waypoints should there be per path */
	private static final int PATH_LENGTH = 1;
	private Coord lastWaypoint;

	/** how heavy are the tail gonna be */
	public static final String ALPHA = "alpha";
	public static final double DEFAULT_ALPHA = 2.5;

	public static final String XM = "xm";
	public static final double DEFAULT_XM = 1;

	private double xm, alpha;


	public LevyFlight(Settings settings) {
		super(settings);
		if (settings.contains(ALPHA)) {
			this.alpha = settings.getDouble(ALPHA);
		} else {
			this.alpha = DEFAULT_ALPHA;
		}

		if (settings.contains(XM)) {
			this.xm = settings.getDouble(XM);
		} else {
			this.xm = DEFAULT_XM;
		};
	}

	protected LevyFlight(LevyFlight rwp) {
		super(rwp);
		this.xm = rwp.xm;
		this.alpha = rwp.alpha;
	}
	
	/**
	 * Returns a possible (random) placement for a host
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
		Coord c = lastWaypoint;

		c = levyWalk();
		System.out.println(c);
		p.addWaypoint(c);
		
		this.lastWaypoint = c;
		return p;
	}
	
	@Override
	public LevyFlight replicate() {
		return new LevyFlight(this);
	}

	protected Coord randomCoord() {
		return new Coord(rng.nextDouble() * getMaxX(),
				rng.nextDouble() * getMaxY());
	}

	protected Coord levyWalk() {
		double next_X = 0, next_Y = 0;
		do {
			double step_length = pareto();
			double theta = rng.nextDouble(0, 2 * Math.PI);

			next_X = lastWaypoint.getX() + step_length * Math.cos(theta);
			next_Y = lastWaypoint.getY() + step_length * Math.sin(theta);

			System.out.println("X" + next_X + " Y" + next_Y);

		} while (next_X >= getMaxX() || next_Y >= getMaxY() || next_X <= 0 || next_Y <= 0);

		return new Coord(next_X, next_Y);
	}

	/**
	 * Metode for calculate the pareto number
	 */
	public double pareto() {
		double u = 1 - rng.nextDouble();
		return (this.xm / Math.pow(u, (1 / this.alpha)));
	}
}
