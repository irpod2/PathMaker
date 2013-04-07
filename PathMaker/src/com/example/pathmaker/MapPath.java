
package com.example.pathmaker;

import java.util.ArrayList;

public class MapPath
{
	private ArrayList<Waypoint> waypoints;

	/*
	 * A path consists of a list of Waypoints. The first element is considered
	 * to be the root. Each Waypoint contains a list of Edges (basically just
	 * integer wrappers) that have a value which corresponds to the id of the
	 * Waypoint it is connected to. This design choice was due to the fact that
	 * using pointers to waypoints directly caused Serialization to overflow the
	 * stack. Ids also made keeping track of integrating paths easier. To follow
	 * the path, simply start at the root and follow the edge ids until the
	 * destination is reached.
	 */

	public MapPath(Waypoint root)
	{
		root.path = this;
		root.id = 0;
		waypoints = new ArrayList<Waypoint>();
		waypoints.add(root);
	}

	public MapPath(ArrayList<Waypoint> points)
	{
		waypoints = points;
		for (Waypoint wp : waypoints)
			wp.path = this;
	}

	public String serialize()
	{
		String me = "<";
		for (Waypoint wp : waypoints)
		{
			me += wp.serialize();
		}
		me += ">";
		return me;
	}

	public static MapPath createFromString(String pathString)
	{
		try
		{
			if (pathString.charAt(0) == '<')
			{
				ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
				String wpString = pathString.substring(1);
				while (wpString.charAt(0) != '>')
				{
					if (wpString.charAt(0) == '{')
					{
						int closeBrace = wpString.indexOf('}') + 1;
						Waypoint wp = Waypoint.createFromString(wpString
								.substring(0, closeBrace));
						// If no problem with string, add waypoint
						if (wp != null)
							waypoints.add(wp);
						// Otherwise, entire map is compromised
						else
							return null;
						wpString = wpString.substring(closeBrace);
					}
					else
						return null;
				}
				MapPath path = new MapPath(waypoints);
				return path;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	// Resizes points to appear proportional(ish) on any window aspect ratio
	/*
	 * public void resize(float camWidth, float camHeight) { float dw = camWidth
	 * / cameraWidth; float dh = camHeight / cameraHeight; for (Waypoint wp :
	 * waypoints) { wp.x *= dw; wp.y *= dh; }
	 * 
	 * cameraWidth = camWidth; cameraHeight = camHeight; }
	 */

	public int size()
	{
		return waypoints.size();
	}

	public Waypoint getRoot()
	{
		return waypoints.get(0);
	}

	// Adding a new waypoint involves setting its ID and linking it to the path
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

	// Adds all waypoints in oldPath to this path
	// Because it's all linear, adding the current list size to the nodes of the
	// old path doesn't unlink anything
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
