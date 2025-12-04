package report;

import core.*;
import routing.DecisionEngineRouter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SearchingCoverageOverTimeReport extends Report implements UpdateListener {

	private List<Tuple<Integer, Integer>> discoverOT;
	private int timeInterval, lastUpdateTime;

	private SearchingAgentReporting representative;
	private int totalSearchableNodes;

	public SearchingCoverageOverTimeReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();
		this.discoverOT = new LinkedList<>();
		this.timeInterval = 3600;
		this.lastUpdateTime = 0;

		/* Retrieves all searching agent routers */
		final List<SearchingAgentReporting> searchingAgents = SimScenario.getInstance()
				.getHosts()
				.stream()
				.filter(host -> host.getName().startsWith("A"))
				.filter(host -> host.getRouter() instanceof DecisionEngineRouter der && der.getDecisionEngine() instanceof SearchingAgentReporting)
				.map(host -> (SearchingAgentReporting) ((DecisionEngineRouter) host.getRouter()).getDecisionEngine())
				.toList();
		/* Helper representative node */
		this.representative = searchingAgents.getFirst();

		assert this.representative != null : "No searching agents found in the scenario.";

		/* Target identifiers of searchable nodes */
		final String targetPrefix;

		targetPrefix = representative.getTargetPrefix();

		/* Retrieves all searchable nodes according to the target parameters */
		final List<DTNHost> searchableNodes = SimScenario.getInstance()
				.getHosts()
				.stream()
				.filter(host -> host.getName().startsWith(targetPrefix))
				.toList();

		/* Check if we have any searchable nodes */
		if (searchableNodes.isEmpty()) {
			System.err.print("No searchable nodes found in the scenario.");
			return;
		}

		this.totalSearchableNodes = searchableNodes.size();
	}

	@Override
	public void updated(List<DTNHost> hosts) {
		/** already needs to update */
		if ((SimClock.getIntTime() - this.lastUpdateTime) >= this.timeInterval) {
			this.discoverOT.add(new Tuple(SimClock.getIntTime(), this.representative.getDiscoveredNodes().size()));
			this.lastUpdateTime = SimClock.getIntTime();
		}
	}

	@Override
	public void done() {
		this.discoverOT.add(new Tuple(SimClock.getIntTime(), this.representative.getDiscoveredNodes().size()));

		write("time" + "," + "numberOfDiscoveredNodes" + "," + "percentage");
		for (Tuple<Integer, Integer> tuple : discoverOT) {
			write(tuple.getKey() + "," + tuple.getValue() + "," + (double) tuple.getValue() / this.totalSearchableNodes * 100);
		}

		super.done();
	}
}
