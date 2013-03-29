
package com.example.pathmaker;

import java.io.Serializable;
import java.util.ArrayList;

public class MapPath implements Serializable
{
	private static final long serialVersionUID = -983318575744762813L;

	private ArrayList<Waypoint> waypoints;

	private float cameraWidth;
	private float cameraHeight;

	public MapPath(Waypoint root, float camWidth, float camHeight)
	{
		cameraWidth = camWidth;
		cameraHeight = camHeight;
		root.path = this;
		root.id = 0;
		waypoints = new ArrayList<Waypoint>();
		waypoints.add(root);
	}

	public void resize(float camWidth, float camHeight)
	{
		float dw = camWidth / cameraWidth;
		float dh = camHeight / cameraHeight;
		for (Waypoint wp : waypoints)
		{
			wp.x *= dw;
			wp.y *= dh;
		}

		cameraWidth = camWidth;
		cameraHeight = camHeight;
	}

	public int size()
	{
		return waypoints.size();
	}

	public Waypoint getRoot()
	{
		return waypoints.get(0);
	}

	public void addWaypoint(Waypoint wp)
	{
		wp.path = this;
		wp.id = waypoints.size();
		waypoints.add(wp);
	}

	public Waypoint getWaypoint(int id)
	{
		return waypoints.get(id);
	}

	public void integrate(MapPath oldPath)
	{
		int reassignEdgesFrom = waypoints.size();
		for (Waypoint wp : oldPath.waypoints)
		{
			addWaypoint(wp);
			wp.addToEdgeIds(reassignEdgesFrom);
		}
		oldPath.waypoints.clear();
	}
}
