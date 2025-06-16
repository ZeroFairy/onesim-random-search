package movement;

import core.Coord;
import core.Settings;

import java.util.*;

/**
 * Generated clustered spawns using Thomas Cluster Point Process Model.
 *
 * @author Jordan, Nara
 */
public class StationaryClustered extends StationaryNodes {
	/**
	 * Number of clusters
	 */
	public static final String CLUSTERPOI_NS = "cluster";

	/**
	 * Controls how heavy the distribution tail is
	 */
	public static final String ALPHA_NS = "alpha";
	public static final double DEFAULT_ALPHA = 0.5;

	/**
	 * Controls the scaling of the pareto
	 */
	public static final String XM_NS = "xm";
	public static final double DEFAULT_XM = 1.0;

	/**
	 * Controls the spread of the clusters, smaller is denser
	 */
	public static final String SIGMA_NS = "sigma";
	public static final double[] DEFAULT_SIGMA = new double[]{3.0, 3.0};

	protected static int nrofCluster;
	protected static List<Coord> POICoords;

	private final double xm;
	private final double alpha;
	private final double[] sigma;
	private Coord location;

	public StationaryClustered(Settings s) {
		super(s);

		if (s.contains(CLUSTERPOI_NS)) {
			int[] clusterRange = s.getCsvInts(CLUSTERPOI_NS); // Group2.cluster = 10, 10
			nrofCluster = Math.min(
				clusterRange[1],
				(int) Math.ceil(rng.nextDouble(clusterRange[0], clusterRange[1] + 1)));
		} else nrofCluster = 3;

		this.alpha = s.contains(ALPHA_NS) ? s.getDouble(ALPHA_NS) : DEFAULT_ALPHA;
		this.xm = s.contains(XM_NS) ? s.getDouble(XM_NS) : DEFAULT_XM;

		if (s.contains(SIGMA_NS)) {
			sigma = s.getCsvDoubles(SIGMA_NS);
		} else sigma = DEFAULT_SIGMA;

		POICoords = new ArrayList<>();
	}

	public StationaryClustered(StationaryClustered sc) {
		super(sc);
		this.location = sc.location;
		this.alpha = sc.alpha;
		this.xm = sc.xm;
		this.sigma = sc.sigma;
	}

	/**
	 * Returns the only location of this movement model
	 *
	 * @return the only location of this movement model
	 */
	@Override
	public Coord getInitialLocation() {
		if (POICoords.isEmpty()) {
			this.generateParentCoord();
		}

		/* Selects POI coordinate (centroid) using pareto */
		Coord currentPOI = POICoords.get((int) ((pareto() * 100) % POICoords.size()));

		double x;
		double y;
		do {
			/* We pick a random value from a defined sigma */
			double randomizedSigma = rng.nextDouble(sigma[0], sigma[1] + 1);
			double distance = rayleighDistance(randomizedSigma);

			/* Calculating a random direction (circle) */
			double theta = rng.nextDouble() * 2 * Math.PI;

			/* Calculate the next X and Y according to the direction from the POI */
			x = currentPOI.getX() + distance * Math.cos(theta);
			y = currentPOI.getY() + distance * Math.sin(theta);

		} while (x > getMaxX() || y > getMaxY() || x < 0 || y < 0);
		return new Coord(x, y);
	}

	/**
	 * Rayleigh Distribution, used to model natural distance from a centerpoint.
	 */
	private double rayleighDistance(double sigma) {
		double u = rng.nextDouble();
		return sigma * Math.sqrt(-2 * Math.log(1 - u));
	}


	@Override
	public StationaryClustered replicate() {
		return new StationaryClustered(this);
	}

	private void generateParentCoord() {
		for (int i = 0; i < nrofCluster; i++) {
			POICoords.add(randomCoord());
		}

		System.out.println("Number of POI: " + POICoords.size());
	}

	protected Coord randomCoord() {
		return new Coord(rng.nextDouble() * getMaxX(),
			rng.nextDouble() * getMaxY());
	}

	/**
	 * Calculates a step length based on the Pareto distribution.
	 *
	 * @return A value representing the step length
	 */
	public double pareto() {
		double u = 1 - rng.nextDouble(); // Ensures u is in (0, 1]
		return this.xm / Math.pow(u, 1 / this.alpha);
	}
}
