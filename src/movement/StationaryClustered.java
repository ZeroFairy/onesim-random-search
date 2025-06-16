package movement;

import core.Coord;
import core.Settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generated Thomas Cluster Process Model.
 *
 * @author jordan
 */
public class StationaryClustered extends StationaryNodes {
	/** Number of cluster */
	public static final String	NROF_CLUSTER = "nrofCluster";

	/** Controls how heavy the distribution tail is */
	public static final String ALPHA = "alpha";
	public static final double DEFAULT_ALPHA = 0.5;

	public static final String XM = "xm";
	public static final double DEFAULT_XM = 1;

	private double xm, alpha;
	public static int nrofCluster;
	public static Set<Coord> parentCoord;
	private Coord location;

	public StationaryClustered(Settings s) {
		super(s);

		if (s.contains(NROF_CLUSTER)) {
			this.nrofCluster = s.getInt(NROF_CLUSTER);
		} else {
			this.nrofCluster = 5;
		}
		this.alpha = s.contains(ALPHA) ? s.getDouble(ALPHA) : DEFAULT_ALPHA;
		this.xm = s.contains(XM) ? s.getDouble(XM) : DEFAULT_XM;

		this.parentCoord = new HashSet<Coord>();
	}

	public StationaryClustered(StationaryClustered sp) {
		super(sp);
		this.location = sp.location;
		this.alpha = sp.alpha;
		this.xm = sp.xm;

		//apakah butuh
		this.parentCoord = sp.parentCoord;
	}

	/**
	 * Returns the only location of this movement model
	 *
	 * @return the only location of this movement model
	 */
	@Override
	public Coord getInitialLocation() {
		if (this.parentCoord.isEmpty()) {
			this.generateParentCoord();
		}

		List<Coord> parentCoordList = new ArrayList<Coord>(this.parentCoord);
		Coord parent = parentCoordList.get(pareto());

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
	public StationaryClustered replicate() {
		return new StationaryClustered(this);
	}

	private void generateParentCoord() {
		for (int i = 0; i < nrofCluster; i++) {
			this.parentCoord.add(randomCoord());
		}
	}

	protected Coord randomCoord() {
		return new Coord(rng.nextDouble() * getMaxX(),
				rng.nextDouble() * getMaxY());
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
