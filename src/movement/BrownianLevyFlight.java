package movement;

import core.Coord;
import core.Settings;

/**
 * "Brownian" Lévy flight movement, adapted from Lévy flight model.
 * Uses Brownian step instead of Lévy's step on a specified threshold ε.
 *
 * @author narwa
 * @see <a href="https://en.wikipedia.org/wiki/L%C3%A9vy_flight">Lévy flight</a>
 * @see <a href="https://ieeexplore.ieee.org/document/5750071">On the Levy-Walk Nature of Human Mobility</a>
 */
public class BrownianLevyFlight extends MovementModel {
	/**
	 * Number of waypoints per path
	 */
	private static final int PATH_LENGTH = 1;
	private Coord location;

	/**
	 * Controls how heavy the distribution tail is
	 */
	public static final String ALPHA = "BLF_levyAlpha";
	public static final double DEFAULT_ALPHA = 1.5;

	public static final String XM = "BLF_xm";
	public static final double DEFAULT_XM = 1;

	private double xm, alpha;

	/**
	 * Brownian Properties
	 * Namespace for diffusion coefficient setting.
	 */
	private static final String DIFFUSION_COEFF_S = "BLF_diffusionCoefficient";
	/**
	 * Namespace for time step setting.
	 */
	private static final String TIME_STEP_S = "BLF_timeStep";
	/**
	 * (D) Variance of the step sizes, higher value means larger step sizes.
	 */
	protected double diffusionCoefficient;
	/**
	 * Time step (Δt), the lesser value the more accurate (to particles).
	 */
	protected double timeStep;
	/**
	 * Standard deviation for the time steps.
	 */
	protected double stepStdDev;

	/**
	 * Brownian Threshold
	 */
	private static final String BROWNIAN_THRESHOLD_S = "BLF_epsilon";
	/**
	 * Threshold of the levy-brownian per step movement
	 */
	protected double epsilon;

	public BrownianLevyFlight(Settings s) {
		super(s);

		// Initialize Levy Flight parameters
		this.alpha = s.contains(ALPHA) ? s.getDouble(ALPHA) : DEFAULT_ALPHA;
		this.xm = s.contains(XM) ? s.getDouble(XM) : DEFAULT_XM;

		// Initialize Brownian Motion parameters
		if (s.contains(DIFFUSION_COEFF_S)) {
			this.diffusionCoefficient = s.getDouble(DIFFUSION_COEFF_S);
		} else {
			this.diffusionCoefficient = 1.0;
		}

		if (s.contains(TIME_STEP_S)) {
			this.timeStep = s.getDouble(TIME_STEP_S);
		} else {
			this.timeStep = 0.1;
		}

		// Formula: σ = √(2D·Δt)
		this.stepStdDev = Math.sqrt(2 * diffusionCoefficient * timeStep);

		// Initialize Brownian Threshold
		this.epsilon = s.contains(BROWNIAN_THRESHOLD_S) ? s.getDouble(BROWNIAN_THRESHOLD_S) : 0.2;
	}

	protected BrownianLevyFlight(BrownianLevyFlight blf) {
		super(blf);
		this.xm = blf.xm;
		this.alpha = blf.alpha;

		this.diffusionCoefficient = blf.diffusionCoefficient;
		this.timeStep = blf.timeStep;
		this.stepStdDev = blf.stepStdDev;

		this.epsilon = blf.epsilon;
	}

	/**
	 * Returns a random initial location for a host on the map.
	 *
	 * @return Random position on the map
	 */
	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		Coord c = randomCoord();
		this.location = c;
		return c;
	}

	@Override
	public Path getPath() {
		Path p = new Path(generateSpeed());
		p.addWaypoint(location.clone());

		Coord c = brownianLevyFlight();
		p.addWaypoint(c);

		this.location = c;
		return p;
	}

	@Override
	public BrownianLevyFlight replicate() {
		return new BrownianLevyFlight(this);
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
	 * Performs a Brownian OR Lévy Flight decision making step to determine the
	 * next coordinate.
	 */
	protected Coord brownianLevyFlight() {
		double nextX, nextY;

		do {
			// generate uniform random for switching decision
			double uRNG = rng.nextDouble(); // This is [0, 1)
			System.out.println("Uniform random for step decision: " + uRNG);

			if (uRNG < this.epsilon) {
				// Brownian step (happens with probability ε)
				double deltaX = rng.nextGaussian() * stepStdDev;
				double deltaY = rng.nextGaussian() * stepStdDev;

				nextX = location.getX() + deltaX;
				nextY = location.getY() + deltaY;
			} else {
				// Lévy flight step (happens with probability 1-ε)
				double stepLength = pareto(); // generate independent step length
				double theta = rng.nextDouble() * 2 * Math.PI;

				nextX = location.getX() + stepLength * Math.cos(theta);
				nextY = location.getY() + stepLength * Math.sin(theta);
			}

		} while (nextX >= getMaxX() || nextY >= getMaxY() || nextX <= 0 || nextY <= 0);

		return new Coord(nextX, nextY);
	}

	/**
	 * Calculates a step length based on the Pareto distribution.
	 *
	 * @return A value representing the step length
	 */
	public double pareto() {
		double u = 1 - rng.nextDouble(); // Ensures u is in (0, 1]
		final double result = this.xm / Math.pow(u, 1 / this.alpha);
		System.out.println("Pareto sample: " + result);
		return result;
	}

	/**
	 * This version of pareto takes a uniform random variable u in (0,1] already
	 * defined by the caller.
	 *
	 * @param u A uniform random variable in (0, 1]
	 * @return A value representing the step length
	 *
	 */
	public double pareto(double u) {
		if (u <= 0 || u > 1) {
			throw new IllegalArgumentException("u must be in (0, 1]");
		}
		return this.xm / Math.pow(u, 1 / this.alpha);
	}
}
