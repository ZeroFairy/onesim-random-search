package report;

import core.DTNHost;
import core.SimScenario;
import routing.DecisionEngineRouter;

import java.util.List;

public class SearchingCoverageReport extends Report {

	public SearchingCoverageReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void done() {
		/* Header with scenario name */
		write("Message stats for scenario " + getScenarioName() +
			"\nsim_time: " + format(getSimTime()));

		/* Retrieves all searching agent routers */
		final List<SearchingAgentReporting> searchingAgents = SimScenario.getInstance()
			.getHosts()
			.stream()
			.filter(host -> host.getName().startsWith("A"))
			.filter(host -> host.getRouter() instanceof DecisionEngineRouter der && der.getDecisionEngine() instanceof SearchingAgentReporting)
			.map(host -> (SearchingAgentReporting) ((DecisionEngineRouter) host.getRouter()).getDecisionEngine())
			.toList();
		/* Helper representative node */
		SearchingAgentReporting representative = searchingAgents.getFirst();

		assert representative != null : "No searching agents found in the scenario.";

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

		final int totalSearchingAgents = searchingAgents.size();
		final int totalSearchableNodes = searchableNodes.size();
		final int totalDiscoveredNodes = representative.getDiscoveredNodes().size();
		final int totalRemainingNodes = totalSearchableNodes - totalDiscoveredNodes;
		final double totalSuccessRate = (double) totalDiscoveredNodes / totalSearchableNodes * 100;
		System.out.printf("totalDisc");

		write(String.format(
			"""
				totalSearchingAgents: %d
				initialDiscoveryTime: %s
				totalSearchableNodes: %d
				totalDiscoveredNodes: %d
				totalRemainingNodes: %s
				totalSuccessRate: %.2f%%
				""",
			totalSearchingAgents,
			searchingAgents.getFirst().getInitialDiscovery(),
			totalSearchableNodes,
			totalDiscoveredNodes,
			totalRemainingNodes,
			totalSuccessRate
		));

		super.done();
	}
}
