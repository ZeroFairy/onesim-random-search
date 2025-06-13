/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package gui.playfield;

import core.Coord;
import core.DTNHost;
import core.World;
import gui.DTNSimGUI;
import lombok.Getter;
import lombok.Setter;
import movement.Path;
import movement.map.SimMap;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * The canvas where node graphics and message visualizations are drawn.
 */
public class PlayField extends JPanel {
	public static final int PLAYFIELD_OFFSET = 10;

	private World w;
	private final DTNSimGUI gui;

	private Color bgColor = Color.WHITE;

	private List<PlayFieldGraphic> overlayGraphics;
	private boolean autoClearOverlay;    // automatically clear overlay graphics
	private MapGraphic mapGraphic;
	@Getter
	@Setter
	private boolean showNodeTrails;
	private boolean showMapGraphic;
	private ScaleReferenceGraphic refGraphic;

	private BufferedImage underlayImage;
	private AffineTransform imageTransform;
	private AffineTransform curTransform;
	private double underlayImgDx;
	private double underlayImgDy;

	private Map<DTNHost, NodeGraphic> activeNodeGraphics;

	/**
	 * Creates a playfield
	 *
	 * @param w The world that contains the actors to be drawn
	 */
	public PlayField(World w, DTNSimGUI gui) {
		this.w = w;
		this.gui = gui;

		this.refGraphic = new ScaleReferenceGraphic();
		updateFieldSize();
		this.setBackground(bgColor);
		this.overlayGraphics = Collections.synchronizedList(
			new ArrayList<PlayFieldGraphic>());
		this.mapGraphic = null;
		this.underlayImage = null;
		this.imageTransform = null;
		this.autoClearOverlay = true;
		this.activeNodeGraphics = new HashMap<>();

		synchronizeNodeGraphics();
	}

	/**
	 * Schedule the play field to be drawn
	 */
	public void updateField() {
		synchronizeNodeGraphics();

		this.repaint();
	}

	/**
	 * Synchronizes the activeNodeGraphics map with the current hosts in the world.
	 * Creates NodeGraphic objects for new hosts and removes them for hosts that no
	 * longer exist.
	 */
	private void synchronizeNodeGraphics() {
		List<DTNHost> currentHosts = w.getHosts();
		Map<DTNHost, NodeGraphic> newActiveNodeGraphics = new HashMap<>();

		for (DTNHost host : currentHosts) {
			if (activeNodeGraphics.containsKey(host)) {
				newActiveNodeGraphics.put(host, activeNodeGraphics.get(host));
			} else {
				newActiveNodeGraphics.put(host, new NodeGraphic(host));
			}
		}
		activeNodeGraphics = newActiveNodeGraphics;
	}

	/**
	 * Sets an image to show under the host graphics
	 *
	 * @param image    The image to set or null to remove the image
	 * @param dx       X offset of the image
	 * @param dy       Y offset of the image
	 * @param scale    Image scaling factor
	 * @param rotation Rotatation angle of the image (radians)
	 */
	public void setUnderlayImage(BufferedImage image,
								 double dx, double dy, double scale, double rotation) {
		if (image == null) {
			this.underlayImage = null;
			this.imageTransform = null;
			this.curTransform = null;
			return;
		}
		this.underlayImage = image;
		this.imageTransform = AffineTransform.getRotateInstance(rotation);
		this.imageTransform.scale(scale, scale);
		this.curTransform = new AffineTransform(imageTransform);
		this.underlayImgDx = dx;
		this.underlayImgDy = dy;

		curTransform.scale(PlayFieldGraphic.getScale(),
			PlayFieldGraphic.getScale());
		curTransform.translate(this.underlayImgDx, this.underlayImgDy);

	}

	/**
	 * Sets the zooming/scaling factor
	 *
	 * @param scale The new scale
	 */
	public void setScale(double scale) {
		PlayFieldGraphic.setScale(scale);
		this.updateFieldSize();
		if (this.imageTransform != null) {
			this.curTransform = new AffineTransform(imageTransform);
			curTransform.scale(scale, scale);
			curTransform.translate(this.underlayImgDx, this.underlayImgDy);
		}
	}

	/**
	 * Sets the source for the map graphics and enables map graphics showing
	 *
	 * @param simMap The map to show
	 */
	public void setMap(SimMap simMap) {
		this.mapGraphic = new MapGraphic(simMap);
		this.showMapGraphic = true;
	}

	/**
	 * Enables/disables showing of map graphics
	 *
	 * @param show True if the map graphics should be shown (false if not)
	 */
	public void setShowMapGraphic(boolean show) {
		this.showMapGraphic = show;
	}

	/**
	 * Enables or disables the automatic clearing of overlay graphics.
	 * If enabled, overlay graphics are cleared every time a new graphics
	 * object is set to be drawn.
	 *
	 * @param clear Auto clear is enabled if this is true, disabled on false
	 */
	public void setAutoClearOverlay(boolean clear) {
		this.autoClearOverlay = clear;
	}

	/**
	 * Draws the play field. To be called by Swing framework or directly if
	 * different context than screen is desired
	 *
	 * @param g The graphics context to draw the field to
	 */
	public void paint(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;

		g2.setBackground(bgColor);

		g2.translate(PLAYFIELD_OFFSET, PLAYFIELD_OFFSET);

		g2.clearRect(-PLAYFIELD_OFFSET, -PLAYFIELD_OFFSET,
			this.getWidth(),
			this.getHeight());

		if (underlayImage != null && curTransform != null) {
			g2.drawImage(underlayImage, curTransform, null);
		}

		if (mapGraphic != null && showMapGraphic) {
			mapGraphic.draw(g2);
		}

		if (activeNodeGraphics != null) {
			for (NodeGraphic ng : activeNodeGraphics.values()) {
				ng.draw(g2);
			}
		}

		if (overlayGraphics != null) {
			synchronized (overlayGraphics) {
				for (PlayFieldGraphic overlay : overlayGraphics) {
					overlay.draw(g2);
				}
			}
		}

		if (refGraphic != null) {
			this.refGraphic.draw(g2);
		}
	}


	/**
	 * Removes all overlay graphics stored to be drawn
	 */
	public void clearOverlays() {
		this.overlayGraphics.clear();
	}

	/**
	 * Adds graphics for message transfer
	 *
	 * @param from Who the message was from
	 * @param to   Who the message was to
	 */
	public void addMessageTransfer(DTNHost from, DTNHost to) {
		autoClear();
		this.overlayGraphics.add(new MessageGraphic(from, to));
	}

	/**
	 * Adds a path to the overlay graphics
	 *
	 * @param path Path to add
	 */
	public void addPath(Path path) {
		autoClear();
		this.overlayGraphics.add(new PathGraphic(path));
		this.updateField();
	}

	/**
	 * Clears overlay graphics if autoclear is requested
	 *
	 * @see #setAutoClearOverlay(boolean)
	 */
	private void autoClear() {
		if (this.autoClearOverlay) {
			this.clearOverlays();
		}
	}

	/**
	 * Returns the graphical presentation location for the given world
	 * location
	 *
	 * @param loc The location to convert
	 * @return Same location in graphics space
	 * @see #getWorldPosition(Coord)
	 */
	public Coord getGraphicsPosition(Coord loc) {
		Coord c = loc.clone();
		c.setLocation(PlayFieldGraphic.scale(c.getX()) + PLAYFIELD_OFFSET,
			PlayFieldGraphic.scale(c.getY()) + PLAYFIELD_OFFSET);
		return c;
	}

	/**
	 * Returns a world location for a given graphical location. Note that
	 * there might be inaccuracies because of rounding.
	 *
	 * @param loc The location to convert
	 * @return Same location in world space
	 * @see #getGraphicsPosition(Coord)
	 */
	public Coord getWorldPosition(Coord loc) {
		Coord c = loc.clone();
		c.setLocation(PlayFieldGraphic.invScale(c.getX() - PLAYFIELD_OFFSET),
			PlayFieldGraphic.invScale(c.getY() - PLAYFIELD_OFFSET));
		return c;
	}

	/**
	 * Updates the playfields (graphical) size to match the world's size
	 * and current scale/zoom.
	 */
	private void updateFieldSize() {
		Dimension minSize = new Dimension(
			PlayFieldGraphic.scale(w.getSizeX()),
			PlayFieldGraphic.scale(w.getSizeY()));
		this.setMinimumSize(minSize);
		this.setPreferredSize(minSize);
		this.setSize(minSize);
	}
}
