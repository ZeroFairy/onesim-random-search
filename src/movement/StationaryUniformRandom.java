package movement;

import core.Coord;
import core.Settings;

/**
 * Generates truly uniform random (homogeneous) stationary distribution.
 * Each point is placed independently with equal probability across the entire map area.
 * This creates a Poisson point process with spatially independent resource placement.
 * 
 * Mathematical properties:
 * - Variance/Mean ratio (Index of Dispersion) ≈ 1
 * - No spatial correlation between points
 * - Follows Poisson distribution: P(k) = (λ^k × e^(-λ)) / k!
 *
 * @author narwa
 */
public class StationaryUniformRandom extends StationaryNodes {

	/**
	 * Optional: minimum distance between points to avoid overlap (in meters)
	 * Set to 0 for pure random (true Poisson process)
	 */
	public static final String MIN_DISTANCE_S = "minDistance";
	public static final double DEFAULT_MIN_DISTANCE = 0.0;

	private final double minDistance;

	public StationaryUniformRandom(Settings s) {
		super(s);
		this.minDistance = s.contains(MIN_DISTANCE_S) ? 
			s.getDouble(MIN_DISTANCE_S) : DEFAULT_MIN_DISTANCE;
	}

	public StationaryUniformRandom(StationaryUniformRandom sur) {
		super(sur);
		this.minDistance = sur.minDistance;
	}

	/**
	 * Returns a uniformly random location across the entire map area.
	 * Each coordinate has equal probability of being selected.
	 *
	 * @return uniformly random coordinate within map bounds
	 */
	@Override
	public Coord getInitialLocation() {
		double x = rng.nextDouble() * getMaxX();
		double y = rng.nextDouble() * getMaxY();
		
		return new Coord(x, y);
	}

	@Override
	public StationaryUniformRandom replicate() {
		return new StationaryUniformRandom(this);
	}

	/**
	 * Calculates Euclidean distance between two coordinates.
	 * Useful if implementing minimum distance constraints.
	 */
	private double distance(Coord c1, Coord c2) {
		double dx = c1.getX() - c2.getX();
		double dy = c1.getY() - c2.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}
}