/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.*;
import movement.MovementModel;
import report.SearchingAgentReporting;

import java.util.*;

/**
 * Router class for searching agents where it searches a nodes with specific parameters.
 *
 * @author Jordan, Nara
 */
public class SearchingAgentRouter implements RoutingDecisionEngine, SearchingAgentReporting {
	/**
	 * The movement model class name of the target nodes.
	 */
	public static String TARGET_MOVEMENT_S = "targetMovement";
	/**
	 * The address prefix of the target nodes.
	 */
	public static String TARGET_PREFIX_S = "targetPrefix";
	/**
	 * The movement model class name of the target nodes.
	 */
	public static String TARGET_ROUTER_S = "targetRouter";

	public static String searchingAgentPrefix = "A";

	protected static Set<DTNHost> discoveredTargets;
	private static double initialDiscovery;

	//    private MessageRouter targetRouter;
	private static MovementModel targetMovement;
	private static String targetPrefix;

	/* Registers the router for reset. */
	static {
		DTNSim.registerForReset(SearchingAgentRouter.class.getCanonicalName());
		reset();
	}

	/**
	 * Resets the {@link #discoveredTargets} and {@link #initialDiscovery} to its original state}.
	 * This is called when the simulation is reset.
	 */
	public static void reset() {
		discoveredTargets = new HashSet<>();
		initialDiscovery = 0;
	}

	public SearchingAgentRouter(Settings s) {
		s.setNameSpace("SearchingAgentRouter");
//        if (s.contains(TARGET_ROUTER_S)) {
//            try {
//                targetRouter = Class.forName(TARGET_ROUTER_S);
//            } catch (ClassNotFoundException ex) {
//                throw new SettingsError("router lu salah gblk");
//            }
//        } else {
//            targetRouter = null;
//        }
		if (s.contains(TARGET_MOVEMENT_S)) {
			targetMovement = (MovementModel) s.createIntializedObject("movement." + s.getSetting(TARGET_MOVEMENT_S));
		} else {
			targetMovement = null;
		}
		if (s.contains(TARGET_PREFIX_S)) {
			targetPrefix = s.getSetting(TARGET_PREFIX_S);
		} else {
			targetPrefix = "S";
		}

		if (discoveredTargets == null) {
			discoveredTargets = new HashSet<>();
		}
	}

	public SearchingAgentRouter(SearchingAgentRouter sar) {
		super();
	}

	/**
	 * Determines whether the peer is the target nodes to be searched,
	 * then inserts it into {@link #discoveredTargets}.
	 */
	@Override
	public void connectionUp(DTNHost thisHost, DTNHost peer) {
		/* Ignore same routers / of same group */
		if (peer.getGroupId().startsWith(searchingAgentPrefix) || peer.getGroupId().equals(thisHost.getGroupId()) || peer.getRouter() == thisHost.getRouter()) {
			return;
		}
		if (targetMovement != null && targetMovement.getClass().isInstance(peer.getMovement())) {
			System.out.println("FOUND HOST MOV: " + peer.getName());
			if (initialDiscovery == 0) {
				initialDiscovery = SimClock.getTime();
			}
			discoveredTargets.add(peer);
		}
//        else if (targetRouter != null && targetRouter.isInstance(peer.getRouter())) {
//            System.out.println("FOUND HOST: " + peer.getAddress());
//            if (initialDiscovery == 0) {
//                initialDiscovery = SimClock.getTime();
//            }
//            encounteredSearchables.add(peer);
//        }
		else if (peer.getName().startsWith(targetPrefix)) {
			System.out.println("FOUND HOST PRE: " + peer.getName());
			if (initialDiscovery == 0) {
				initialDiscovery = SimClock.getTime();
			}
			discoveredTargets.add(peer);
		}
	}

	@Override
	public void connectionDown(DTNHost thisHost, DTNHost peer) {
	}

	@Override
	public void doExchangeForNewConnection(Connection con, DTNHost peer) {
	}

	@Override
	public boolean newMessage(Message m) {
		return true;
	}

	@Override
	public boolean isFinalDest(Message m, DTNHost aHost) {
		return m.getTo() == aHost;
	}

	@Override
	public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
		return !thisHost.getRouter().hasMessage(m.getId());
	}

	@Override
	public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
		return true;
	}

	@Override
	public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
		return false;
	}

	@Override
	public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
		return false;
	}

	@Override
	public RoutingDecisionEngine replicate() {
		return new SearchingAgentRouter(this);
	}

	@Override
	public void update(DTNHost thisHost) {
	}

	/**
	 * Checks whether a host router is of the same class as target
	 */
	public static boolean isTargetRouter(DTNHost host, String targetRouter) {
		MessageRouter hostRouter = host.getRouter();
		try {
			Class<?> targetClass = Class.forName(targetRouter);
			return targetClass.isAssignableFrom(hostRouter.getClass());
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/* SearchingAgentReporting implementations */

	@Override
	public double getInitialDiscovery() {
		return initialDiscovery;
	}

	@Override
	public Collection<DTNHost> getDiscoveredNodes() {
		return discoveredTargets;
	}

	@Override
	public String getTargetPrefix() {
		return targetPrefix;
	}
}
