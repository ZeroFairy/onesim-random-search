/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import java.awt.*;
import java.util.*;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import movement.MovementModel;
import movement.Path;
import routing.MessageRouter;
import routing.RoutingInfo;

// tambahan
import routing.community.Duration;

/**
 * A DTN capable host.
 */
public class DTNHost implements Comparable<DTNHost> {

    private static int nextAddress = 0;
    private int address;

    private Coord location; 	// where is the host
    private Coord destination;	// where is it going

    private MessageRouter router;
    @Getter private MovementModel movement;
    private Path path;
    private double speed;
    private double nextTimeToMove;
    @Getter private String name;
    private int[] color;
    private List<MessageListener> msgListeners;
    private List<MovementListener> movListeners;
    private List<NetworkInterface> net;
    private ModuleCommunicationBus comBus;

    // tambahan testing
    public List<Duration> intervals;
    public List<Double> congestionRatio = new ArrayList<Double>();
	public List<Double> dataInContact = new ArrayList<Double>();
    public List<Double> ema = new ArrayList<Double>();
    public List<Double> dummyForReward = new ArrayList<Double>();

    public Map<Duration, Integer> dataReceivedInDuration;
    public Map<Duration, Integer> dataTransferredInDuration;
    public int msgReceived;
    public int msgTransferred;
    public Set<DTNHost> setofHosts;
    public Map<DTNHost, Duration> durPerNode;
    public Map<DTNHost, List<Duration>> listDurPerNode;
    public double totalContactTime = 0;
        
    static {
        DTNSim.registerForReset(DTNHost.class.getCanonicalName());
        reset();
    }

    /**
     * Creates a new DTNHost.
     *
     * @param msgLs Message listeners
     * @param movLs Movement listeners
     * @param groupId GroupID of this host
     * @param interf List of NetworkInterfaces for the class
     * @param comBus Module communication bus object
     * @param mmProto Prototype of the movement model of this host
     * @param mRouterProto Prototype of the message router of this host
     */
    public DTNHost(List<MessageListener> msgLs,
            List<MovementListener> movLs,
            String groupId, List<NetworkInterface> interf,
            ModuleCommunicationBus comBus,
            MovementModel mmProto, MessageRouter mRouterProto) {
        this.comBus = comBus;
        this.location = new Coord(0, 0);
        this.address = getNextAddress();
        this.name = groupId + address;
        this.net = new ArrayList<NetworkInterface>();

        for (NetworkInterface i : interf) {
            NetworkInterface ni = i.replicate();
            ni.setHost(this);
            net.add(ni);
        }

        // TODO - think about the names of the interfaces and the nodes
        //this.name = groupId + ((NetworkInterface)net.get(1)).getAddress();
        this.msgListeners = msgLs;
        this.movListeners = movLs;

        // create instances by replicating the prototypes
        this.movement = mmProto.replicate();
        this.movement.setComBus(comBus);
        setRouter(mRouterProto.replicate());

        this.location = movement.getInitialLocation();

        this.nextTimeToMove = movement.nextPathAvailable();
        this.path = null;

        if (movLs != null) { // inform movement listeners about the location
            for (MovementListener l : movLs) {
                l.initialLocation(this, this.location);
            }
        }

        // tambahan
        // this.setNode = new HashSet<DTNHost>();
        this.intervals = new LinkedList<Duration>();
        this.dataReceivedInDuration = new HashMap<Duration, Integer>();
        this.msgReceived = 0;
        this.msgTransferred = 0;
        this.setofHosts = new HashSet<DTNHost>();
        this.durPerNode = new HashMap<DTNHost, Duration>();
        this.listDurPerNode = new HashMap<DTNHost, List<Duration>>();
        // this.ema = new ArrayList<Double>();
        // this.ema.add(0.0);
    }

    /**
     * Returns a new network interface address and increments the address for
     * subsequent calls.
     *
     * @return The next address.
     */
    private synchronized static int getNextAddress() {
        return nextAddress++;
    }

    /**
     * Reset the host and its interfaces
     */
    public static void reset() {
        nextAddress = 0;
    }

    /**
     * Returns true if this node is active (false if not)
     *
     * @return true if this node is active (false if not)
     */
    public boolean isActive() {
        return this.movement.isActive();
    }

    /**
     * Set a router for this host
     *
     * @param router The router to set
     */
    private void setRouter(MessageRouter router) {
        router.init(this, msgListeners);
        this.router = router;
    }

    /**
     * Returns the router of this host
     *
     * @return the router of this host
     */
    public MessageRouter getRouter() {
        return this.router;
    }

    /**
     * Returns the network-layer address of this host.
     */
    public int getAddress() {
        return this.address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * Returns this hosts's ModuleCommunicationBus
     *
     * @return this hosts's ModuleCommunicationBus
     */
    public ModuleCommunicationBus getComBus() {
        return this.comBus;
    }

    /**
     * Informs the router of this host about state change in a connection
     * object.
     *
     * @param con The connection object whose state changed
     */
    public void connectionUp(Connection con) {
        this.router.changedConnection(con);
    }

    public void connectionDown(Connection con) {
        this.router.changedConnection(con);
    }

    /**
     * Returns a copy of the list of connections this host has with other hosts
     *
     * @return a copy of the list of connections this host has with other hosts
     */
    public List<Connection> getConnections() {
        List<Connection> lc = new ArrayList<Connection>();

        for (NetworkInterface i : net) {
            lc.addAll(i.getConnections());
        }

        return lc;
    }

    /**
     * Returns the current location of this host.
     *
     * @return The location
     */
    public Coord getLocation() {
        return this.location;
    }

    /**
     * Returns the Path this node is currently traveling or null if no path is
     * in use at the moment.
     *
     * @return The path this node is traveling
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Sets the Node's location overriding any location set by movement model
     *
     * @param location The location to set
     */
    public void setLocation(Coord location) {
        this.location = location.clone();
    }

    /**
     * Sets the Node's name overriding the default name (groupId + netAddress)
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the Node's color overriding the default color
     *  
     * @param color The color to set
     */
    public void setColor(int[] color) {
        this.color = color;
    }

    /**
     * Returns the messages in a collection.
     *
     * @return Messages in a collection
     */
    public Collection<Message> getMessageCollection() {
        return this.router.getMessageCollection();
    }

    /**
     * Returns the number of messages this node is carrying.
     *
     * @return How many messages the node is carrying currently.
     */
    public int getNrofMessages() {
        return this.router.getNrofMessages();
    }

    /**
     * Returns the buffer occupancy percentage. Occupancy is 0 for empty buffer
     * but can be over 100 if a created message is bigger than buffer space that
     * could be freed.
     *
     * @return Buffer occupancy percentage
     */
    public double getBufferOccupancy() {
        double bSize = router.getBufferSize();
        double freeBuffer = router.getFreeBufferSize();
        return 100 * ((bSize - freeBuffer) / bSize);
    }

    /**
     * Returns routing info of this host's router.
     *
     * @return The routing info.
     */
    public RoutingInfo getRoutingInfo() {
        return this.router.getRoutingInfo();
    }

    /**
     * Returns the interface objects of the node
     */
    public List<NetworkInterface> getInterfaces() {
        return net;
    }

    /**
     * Find the network interface based on the index
     */
    protected NetworkInterface getInterface(int interfaceNo) {
        NetworkInterface ni = null;
        try {
            ni = net.get(interfaceNo - 1);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("No such interface: " + interfaceNo);
            System.exit(0);
        }
        return ni;
    }

    /**
     * Find the network interface based on the interfacetype
     */
    protected NetworkInterface getInterface(String interfacetype) {
        for (NetworkInterface ni : net) {
            if (ni.getInterfaceType().equals(interfacetype)) {
                return ni;
            }
        }
        return null;
    }

    /**
     * Force a connection event
     */
    public void forceConnection(DTNHost anotherHost, String interfaceId,
            boolean up) {
        NetworkInterface ni;
        NetworkInterface no;

        if (interfaceId != null) {
            ni = getInterface(interfaceId);
            no = anotherHost.getInterface(interfaceId);

            assert (ni != null) : "Tried to use a nonexisting interfacetype " + interfaceId;
            assert (no != null) : "Tried to use a nonexisting interfacetype " + interfaceId;
        } else {
            ni = getInterface(1);
            no = anotherHost.getInterface(1);

            assert (ni.getInterfaceType().equals(no.getInterfaceType())) :
                    "Interface types do not match.  Please specify interface type explicitly";
        }

        if (up) {
            ni.createConnection(no);
        } else {
            ni.destroyConnection(no);
        }
    }

    /**
     * for tests only --- do not use!!!
     */
    public void connect(DTNHost h) {
        System.err.println(
                "WARNING: using deprecated DTNHost.connect(DTNHost)"
                + "\n Use DTNHost.forceConnection(DTNHost,null,true) instead");
        forceConnection(h, null, true);
    }

    /**
     * Updates node's network layer and router.
     *
     * @param simulateConnections Should network layer be updated too
     */
    public void update(boolean simulateConnections) {
        if (!isActive()) {
            return;
        }

        if (simulateConnections) {
            for (NetworkInterface i : net) {
                i.update();
            }
        }
        this.router.update();
    }

    /**
     * Moves the node towards the next waypoint or waits if it is not time to
     * move yet
     *
     * @param timeIncrement How long time the node moves
     */
    public void move(double timeIncrement) {
        double possibleMovement;
        double distance;
        double dx, dy;

        if (!isActive() || SimClock.getTime() < this.nextTimeToMove) {
            return;
        }
        if (this.destination == null) {
            if (!setNextWaypoint()) {
                return;
            }
        }

        possibleMovement = timeIncrement * speed;
        distance = this.location.distance(this.destination);

        while (possibleMovement >= distance) {
            // node can move past its next destination
            this.location.setLocation(this.destination); // snap to destination
            possibleMovement -= distance;
            if (!setNextWaypoint()) { // get a new waypoint
                return; // no more waypoints left
            }
            distance = this.location.distance(this.destination);
        }

        // move towards the point for possibleMovement amount
        dx = (possibleMovement / distance) * (this.destination.getX()
                - this.location.getX());
        dy = (possibleMovement / distance) * (this.destination.getY()
                - this.location.getY());
        this.location.translate(dx, dy);
    }

    /**
     * Sets the next destination and speed to correspond the next waypoint on
     * the path.
     *
     * @return True if there was a next waypoint to set, false if node still
     * should wait
     */
    private boolean setNextWaypoint() {
        if (path == null) {
            path = movement.getPath();
        }

        if (path == null || !path.hasNext()) {
            this.nextTimeToMove = movement.nextPathAvailable();
            this.path = null;
            return false;
        }

        this.destination = path.getNextWaypoint();
        this.speed = path.getSpeed();

        if (this.movListeners != null) {
            for (MovementListener l : this.movListeners) {
                l.newDestination(this, this.destination, this.speed);
            }
        }

        return true;
    }

    /**
     * Sends a message from this host to another host
     *
     * @param id Identifier of the message
     * @param to Host the message should be sent to
     */
    public void sendMessage(String id, DTNHost to) {
        this.router.sendMessage(id, to);
    }

    /**
     * Start receiving a message from another host
     *
     * @param m The message
     * @param from Who the message is from
     * @return The value returned by
     * {@link MessageRouter#receiveMessage(Message, DTNHost)}
     */
    public int receiveMessage(Message m, DTNHost from) {
        int retVal = this.router.receiveMessage(m, from);

        if (retVal == MessageRouter.RCV_OK) {
            m.addNodeOnPath(this);	// add this node on the messages path
        }

        return retVal;
    }

    /**
     * Requests for deliverable message from this host to be sent trough a
     * connection.
     *
     * @param con The connection to send the messages trough
     * @return True if this host started a transfer, false if not
     */
    public boolean requestDeliverableMessages(Connection con) {
        return this.router.requestDeliverableMessages(con);
    }

    /**
     * Informs the host that a message was successfully transferred.
     *
     * @param id Identifier of the message
     * @param from From who the message was from
     */
    public void messageTransferred(String id, DTNHost from) {
        this.router.messageTransferred(id, from);
    }

    /**
     * Informs the host that a message transfer was aborted.
     *
     * @param id Identifier of the message
     * @param from From who the message was from
     * @param bytesRemaining Nrof bytes that were left before the transfer would
     * have been ready; or -1 if the number of bytes is not known
     */
    public void messageAborted(String id, DTNHost from, int bytesRemaining) {
        this.router.messageAborted(id, from, bytesRemaining);
    }

    /**
     * Creates a new message to this host's router
     *
     * @param m The message to create
     */
    public void createNewMessage(Message m) {
        this.router.createNewMessage(m);
    }

    /**
     * Deletes a message from this host
     *
     * @param id Identifier of the message
     * @param drop True if the message is deleted because of "dropping" (e.g.
     * buffer is full) or false if it was deleted for some other reason (e.g.
     * the message got delivered to final destination). This effects the way the
     * removing is reported to the message listeners.
     */
    public void deleteMessage(String id, boolean drop) {
        this.router.deleteMessage(id, drop);
    }

    /**
     * Returns a string presentation of the host.
     *
     * @return Host's name
     */
    public String toString() {
        return name;
    }

    /**
     * Returns the color of this host
     *
     * @return The color of this host
     */
    public int[] getColor() {
        return this.color;
    }

    /**
     * Checks if a host is the same as this host by comparing the object
     * reference
     *
     * @param otherHost The other host
     * @return True if the hosts objects are the same object
     */
    public boolean equals(DTNHost otherHost) {
        return this == otherHost;
    }

    /**
     * Compares two DTNHosts by their addresses.
     *
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(DTNHost h) {
        return this.getAddress() - h.getAddress();
    }

	/**
     * Method tambahan untuk menambah Duration ke list
     */
    public void addDuration(Duration dur) {
        this.intervals.add(dur);
    }

    /**
     * Method tambahan untuk menambah Duration ke list
     */
    // public List<Duration> getNodeIntervals() {
    public String getNodeIntervals() {
        // return this.intervals;
        StringBuilder cek = new StringBuilder("");
        for (Duration d : intervals) {
            // cek += "<" + d.start + ", " + d.end + "> ";
            cek.append("<" + d.start + ", " + d.end + "> ");
        }
        return cek.toString();
    }

}
