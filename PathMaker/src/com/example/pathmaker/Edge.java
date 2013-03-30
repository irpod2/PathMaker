
package com.example.pathmaker;

import java.io.Serializable;

public class Edge implements Serializable
{
	private static final long serialVersionUID = 7911569938578276537L;

	public int edgeToId;

	// Basically just an int wrapper. Corresponds to the connected Waypoint's id
	// in its MapPath.
	public Edge(int id)
	{
		edgeToId = id;
	}
}
