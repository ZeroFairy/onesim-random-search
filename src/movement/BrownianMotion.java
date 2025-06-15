package movement;

import core.Coord;
import core.Settings;

/**
 * Brownian Motion movement.
 * Uses simplified formula (21) from "Asymmetric LW Random Search" paper.
 *
 * @author narwa
 * @see <a href="https://en.wikipedia.org/wiki/Brownian_motion">Brownian Motion</a>
 */
public class BrownianMotion extends MovementModel {
	/**
	 * Namespace for diffusion coefficient setting.
	 */
	private static final String DIFFUSION_COEFF_S = "diffusionCoefficient";
	/**
	 * Namespace for time step setting.
	 */
	private static final String TIME_STEP_S = "timeStep";
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

	protected Coord location;

	public BrownianMotion(Settings s) {
		super(s);

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
	}

	public BrownianMotion(BrownianMotion blf) {
		super(blf);
		this.diffusionCoefficient = blf.diffusionCoefficient;
		this.timeStep = blf.timeStep;
		this.stepStdDev = blf.stepStdDev;
	}

	@Override
	public Path getPath() {
		final Path path = new Path(generateSpeed());
		path.addWaypoint(location.clone());

		Coord nextLocation = getNextBrownian();
//		Coord nextLocation = getNextBrownianDirect(); // IDK the significant difference

		path.addWaypoint(nextLocation);
		location = nextLocation;

		return path;
	}

	/**
	 * Similar approach with previous Lévy flight algorithm (defining a direction with theta).
	 */
	private Coord getNextBrownian() {
		double nextX;
		double nextY;
		do {
			double stepX = rng.nextGaussian() * stepStdDev;
			double stepY = rng.nextGaussian() * stepStdDev;

			/* Calculating a random direction (circle) */
			double theta = rng.nextDouble() * 2 * Math.PI;

			/* Calculate the next X and Y according to the direction */
			nextX = location.getX() + stepX * Math.cos(theta);
			nextY = location.getY() + stepY * Math.sin(theta);
		} while (nextX > getMaxX() || nextY > getMaxY() || nextX < 0 || nextY < 0);
		return new Coord(nextX, nextY);
	}

	/**
	 * Directly implements, not defining direction.
	 */
	private Coord getNextBrownianDirect() {
		double nextX;
		double nextY;
		do {
			double deltaX = rng.nextGaussian() * stepStdDev;
			double deltaY = rng.nextGaussian() * stepStdDev;

			/* Calculate the next X and Y: X(t+Δt) = X(t) + √(2D·Δt) · Z */
			nextX = location.getX() + deltaX;
			nextY = location.getY() + deltaY;
		} while (nextX > getMaxX() || nextY > getMaxY() || nextX < 0 || nextY < 0);
		return new Coord(nextX, nextY);
	}

	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		Coord c = randomCoord();
		this.location = c;
		return c;
	}

	@Override
	public BrownianMotion replicate() {
		return new BrownianMotion(this);
	}

	protected Coord randomCoord() {
		return new Coord(rng.nextDouble() * getMaxX(), rng.nextDouble() * getMaxY());
	}
}
