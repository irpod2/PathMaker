
package com.example.pathmaker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class PathView extends ImageView
{
	private final float MIN_DIST = 30.0f;
	private final float MAX_DIST = 50.0f;
	private final Context context;
	private Paint thinPaint = new Paint();
	private Paint thickPaint = new Paint();
	private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	private MapBundle bundle = new MapBundle();
	private Waypoint lastPoint;
	private Waypoint lastParent;
	private float cameraWidth;
	private float cameraHeight;

	public PathView(Context baseContext, int resourceID, float camWidth,
			float camHeight)
	{
		super(baseContext);

		cameraWidth = camWidth;
		cameraHeight = camHeight;

		context = baseContext;

		setPaints();

		setImageResource(resourceID);
	}

	// thinPaint is used for lines
	// thickPaint is used for points
	private void setPaints()
	{
		thinPaint.setColor(Color.BLUE);
		thinPaint.setStyle(Paint.Style.STROKE);
		thinPaint.setStrokeWidth(8);
		thickPaint.setColor(Color.BLUE);
		thickPaint.setStyle(Paint.Style.STROKE);
		thickPaint.setStrokeWidth(16);
	}

	// Remove all paths and points
	public void clearPath()
	{
		lastPoint = null;
		lastParent = null;
		bundle.paths.clear();
		waypoints.clear();
	}

	// Save all paths using bundle's save method
	public void savePaths(String filename)
	{
		if (!bundle.paths.isEmpty())
		{
			bundle.save(context, filename);
		}
	}

	// Load paths
	public void loadPaths(String filename)
	{
		bundle = MapBundle.load(context, filename);
		if (bundle != null)
		{
			// Add waypoints to View's list (for drawing & searching)
			for (MapPath p : bundle.paths)
			{
				p.resize(cameraWidth, cameraHeight);
				for (int i = 0; i < p.size(); i++)
				{
					waypoints.add(p.getWaypoint(i));
				}
			}
			// Make sure any leftover searches are cleaned up
			unvisitNodes();
		}
		else
			Toast.makeText(context, "Could not load paths", Toast.LENGTH_SHORT)
					.show();
	}

	// Used to avoid TSP when searching
	private void unvisitNodes()
	{
		for (Waypoint wp : waypoints)
		{
			wp.visited = false;
		}
	}

	// Returns (Manhattan) distance between two waypoints
	private float getDistance(Waypoint a, Waypoint b)
	{
		float dx = b.x - a.x;
		float dy = b.y - a.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	// Gets closest point already placed
	private Waypoint getClosestWaypoint(Waypoint wp)
	{
		float minDist = MAX_DIST;
		Waypoint retPt = null;
		for (Waypoint candidate : waypoints)
		{
			if (candidate == lastPoint || candidate == lastParent)
				continue;
			float dist = getDistance(candidate, wp);
			if (dist < minDist)
			{
				retPt = candidate;
				minDist = dist;
			}
		}
		return retPt;
	}

	// Connects two waypoints, and integrates a onto b's path if not already the
	// same. a should always be the new waypoint.
	private void connectPoints(Waypoint a, Waypoint b)
	{
		if (a.path != b.path)
		{
			bundle.paths.remove(a.path);
			// Switch all waypoints in a's path to b's path
			b.path.integrate(a.path);
		}
		a.addConnection(b);
		b.addConnection(a);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		Waypoint wp = new Waypoint((int) e.getX(), (int) e.getY());

		// On touch down, either place a new root or prepare to continue an
		// existing path
		if (e.getAction() == MotionEvent.ACTION_DOWN)
		{
			Waypoint closest = getClosestWaypoint(wp);
			if (closest != null)
			{
				lastPoint = closest;
			}
			else
			{
				bundle.paths.add(new MapPath(wp, cameraWidth, cameraHeight));
				lastPoint = wp;
				waypoints.add(wp);
			}
		}
		// On move, connect new point to previous point once minimum distance
		// between points has been reached
		else if (e.getAction() == MotionEvent.ACTION_MOVE)
		{
			if (getDistance(wp, lastPoint) > MIN_DIST)
			{
				lastPoint.path.addWaypoint(wp);
				connectPoints(wp, lastPoint);

				lastParent = lastPoint;
				lastPoint = wp;
				waypoints.add(wp);
			}
		}
		// On touch up, connect to closest point (under Max distance), not
		// including last two points
		else if (e.getAction() == MotionEvent.ACTION_UP)
		{
			Waypoint closest = getClosestWaypoint(wp);
			if (closest != null)
			{
				connectPoints(lastPoint, closest);
			}
			lastPoint = null;
			lastParent = null;
		}
		return true;
	}

	// Used for color picking: path number determines color
	private int maskBytes(int numBytes)
	{
		int mask = 0xff000000;
		// Ones
		if ((numBytes & 0x00000001) != 0)
			mask |= 0x000000ff;
		// Twos
		if ((numBytes & 0x00000002) != 0)
			mask |= 0x0000ff00;
		// Fours
		if ((numBytes & 0x00000004) != 0)
			mask |= 0x00ff0000;
		return mask;
	}

	// Draws lines between points by following the path
	private void recursiveDrawLine(Canvas canvas, Waypoint wp)
	{
		// Don't visit the same node twice
		wp.visited = true;
		canvas.drawPoint(wp.x, wp.y, thickPaint);
		MapPath path = wp.path;
		for (Edge childEdge : wp.getConnections())
		{
			// Drawing the same line twice is pretty harmless, so I'm not TOO
			// careful here, and it doesn't seem to affect performance
			Waypoint child = path.getWaypoint(childEdge.edgeToId);
			canvas.drawLine(wp.x, wp.y, child.x, child.y, thinPaint);
			if (!child.visited)
			{
				recursiveDrawLine(canvas, child);
			}
		}
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		// Draw each path
		for (int i = 0; i < bundle.paths.size(); i++)
		{
			int color = maskBytes(i % 7 + 1);
			thinPaint.setColor(color);
			thickPaint.setColor(color);
			recursiveDrawLine(canvas, bundle.paths.get(i).getRoot());
		}
		// Reset nodes for next draw cycle
		unvisitNodes();

		// Not sure what this does, but it was in all the online example code
		// snippets for drawing on a view
		invalidate();
	}

	// When the phone is rotated, resize the paths
	public void onOrientationChanged(float camWidth, float camHeight)
	{
		cameraWidth = camWidth;
		cameraHeight = camHeight;
		bundle.resize(camWidth, camHeight);
	}
}
