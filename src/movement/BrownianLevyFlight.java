package movement;

import core.Coord;
import core.Settings;

/**
 * "Brownian" Lévy flight movement, adapted from Lévy flight model.
 * Uses nextGaussian instead of pareto to determine flight length.
 *
 * @see <a href="https://en.wikipedia.org/wiki/L%C3%A9vy_flight">Lévy flight</a>
 * @see <a href="https://ieeexplore.ieee.org/document/5750071">On the Levy-Walk Nature of Human Mobility</a>
 * @author narwa
 */
public class BrownianLevyFlight extends MovementModel {

	protected Coord location;

	public BrownianLevyFlight(Settings s) {
		super(s);
	}

	public BrownianLevyFlight(BrownianLevyFlight blf) {
		super(blf);
	}

	@Override
	public Path getPath() {
		final Path path = new Path(generateSpeed());
		path.addWaypoint(location.clone());

		double nextX;
		double nextY;
		do {
			double step_length = rng.nextGaussian();
//			System.out.printf("Step length: %f\n", step_length);

			/* Calculating a random direction (circle) */
			double theta = rng.nextDouble() * 2 * Math.PI;

			/* Calculate the next X and Y according to the direction */
			nextX = location.getX() + step_length * Math.cos(theta);
			nextY = location.getY() + step_length * Math.sin(theta);
		} while (nextX > getMaxX() || nextY > getMaxY() || nextX < 0 || nextY < 0);

		Coord nextLocation = new Coord(nextX, nextY);
		path.addWaypoint(nextLocation);
		location = nextLocation;

		return path;
	}

	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		Coord c = randomCoord();
		this.location = c;
		return c;
	}

	@Override
	public BrownianLevyFlight replicate() {
		return new BrownianLevyFlight(this);
	}

	protected Coord randomCoord() {
		return new Coord(rng.nextDouble() * getMaxX(), rng.nextDouble() * getMaxY());
	}
}
