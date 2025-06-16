package movement;

import core.Coord;
import core.Settings;

/**
 * Generated random stationary movement.
 *
 * @author narwa
 */
public class StationaryRandom extends StationaryNodes {

	public StationaryRandom(Settings s) {
		super(s);
	}

	public StationaryRandom(StationaryRandom sr) {
		super(sr);
	}

	@Override
	public StationaryRandom replicate() {
		return new StationaryRandom(this);
	}
}
