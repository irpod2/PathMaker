
package com.example.pathmaker;

import java.io.Serializable;
import java.util.ArrayList;

public class Waypoint implements Serializable
{
	private static final long serialVersionUID = -6025388855674604869L;

	private ArrayList<Edge> edges = new ArrayList<Edge>();
	public boolean visited = false;
	public int id;
	public int x;
	public int y;
	public MapPath path;

	public Waypoint(int pX, int pY)
	{
		x = pX;
		y = pY;
		path = null;
	}

	public Waypoint(int pX, int pY, MapPath mapPath)
	{
		x = pX;
		y = pY;
		path = mapPath;
	}

	public void addToEdgeIds(int amount)
	{
		for (Edge e : edges)
			e.edgeToId += amount;
	}

	public void addConnection(Waypoint wp)
	{
		if (!edges.contains(wp.id))
			edges.add(new Edge(wp.id));
	}

	public ArrayList<Edge> getConnections()
	{
		return edges;
	}
}
