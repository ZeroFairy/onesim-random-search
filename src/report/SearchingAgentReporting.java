package report;

import core.DTNHost;

import java.util.Collection;

public interface SearchingAgentReporting {
	/**
	 * Retrieves the time of the first searchable node discovery.
	 * */
	double getInitialDiscovery();
	/**
	 * Retrieves all the discovered searchable nodes.
	 * */
	Collection<DTNHost> getDiscoveredNodes();

	String getTargetPrefix();
}
