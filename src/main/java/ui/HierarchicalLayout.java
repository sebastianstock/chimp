/*
 * Copyright (c) 2017, Sebastian Stock
 * 
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 9, 2005
 * 
 * Adapted edu.uci.ics.jung.algorithms.layout.TreeLayout for the requirements for visualizing CHIMP plans on Nov 7, 2017.
 */

package ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;
import org.metacsp.multi.allenInterval.AllenInterval;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;

public class HierarchicalLayout implements Layout<Fluent, FluentConstraint> {
	
	protected Dimension size = new Dimension(600,600);
	protected Graph<Fluent, FluentConstraint> graph;
	protected Map<Fluent,Integer> basePositions = new HashMap<Fluent,Integer>();
	
    protected Map<Fluent, Point2D> locations = 
        	LazyMap.decorate(new HashMap<Fluent, Point2D>(),
        			new Transformer<Fluent,Point2D>() {
    					public Point2D transform(Fluent arg0) {
    						return new Point2D.Double();
    					}});
	
    protected transient Set<Fluent> alreadyDone = new HashSet<Fluent>();
	
    /**
     * The default horizontal vertex spacing.  Initialized to 50.
     */
    public static int DEFAULT_DISTX = 50;
    
    /**
     * The default vertical vertex spacing.  Initialized to 50.
     */
    public static int DEFAULT_DISTY = 50;
    
    /**
     * The horizontal vertex spacing.  Defaults to {@code DEFAULT_XDIST}.
     */
    protected int distX = 50;
    
    /**
     * The vertical vertex spacing.  Defaults to {@code DEFAULT_YDIST}.
     */
    protected int distY = 50;
    
    protected transient Point m_currentPoint = new Point();
    
    /**
     * Creates an instance for the specified graph with default X and Y distances.
     */
    public HierarchicalLayout(Graph<Fluent,FluentConstraint> g) {
    	this(g, DEFAULT_DISTX, DEFAULT_DISTY);
    }
    
    /**
     * Creates an instance for the specified graph and X distance with
     * default Y distance.
     */
    public HierarchicalLayout(Graph<Fluent,FluentConstraint> g, int distx) {
        this(g, distx, DEFAULT_DISTY);
    }

	public HierarchicalLayout(Graph<Fluent, FluentConstraint> g, int distx, int disty) {
        if (g == null)
            throw new IllegalArgumentException("Graph must be non-null");
        if (distx < 1 || disty < 1)
            throw new IllegalArgumentException("X and Y distances must each be positive");
    	this.graph = g;
        this.distX = distx;
        this.distY = disty;
        buildTree();
	}
	
	protected void buildTree() {
        this.m_currentPoint = new Point(0, 20);
        List<Fluent> roots = getRoots(graph);
        if (roots.size() > 0 && graph != null) {
       		for(Fluent r : sortTasks(roots)) {
        		calculateDimensionX(r);
        		m_currentPoint.x += this.basePositions.get(r)/2 + this.distX;
        		buildTree(r, this.m_currentPoint.x);
        	}
        }
    }
	
    protected void buildTree(Fluent v, int x) {

        if (!alreadyDone.contains(v)) {
            alreadyDone.add(v);

            //go one level further down
            this.m_currentPoint.y += this.distY;
            this.m_currentPoint.x = x;

            this.setCurrentPositionFor(v);

            int sizeXofCurrent = basePositions.get(v);

            int lastX = x - sizeXofCurrent / 2;

            int sizeXofChild;
            int startXofChild;

            for (Fluent element : sortTasks(subtasksOf(graph, v))) {
                sizeXofChild = this.basePositions.get(element);
                startXofChild = lastX + sizeXofChild / 2;
                buildTree(element, startXofChild);
                lastX = lastX + sizeXofChild + distX;
            }
            this.m_currentPoint.y -= this.distY;
        }
    }
    
    private List<Fluent> subtasksOf(Graph<Fluent, FluentConstraint> graph, Fluent task) {
    	List<Fluent> subtasks = new ArrayList<Fluent>();
    	for (FluentConstraint fc : graph.getOutEdges(task)) {
    		if (fc.getType() == FluentConstraint.Type.DC) {
    			subtasks.add((Fluent) fc.getTo());
    		}
    	}
    	return subtasks;
    }
    
    private List<Fluent> sortTasks(List<Fluent> tasks) {
    	Collections.sort(tasks, new Comparator<Fluent>() {

			@Override
			public int compare(Fluent o1, Fluent o2) {
				AllenInterval ai1 = o1.getAllenInterval();
				AllenInterval ai2 = o2.getAllenInterval();
				if (ai1.getEST() < ai2.getEST()) {
					return -1;
				} else if (ai1.getEST() > ai2.getEST()) {
					return 1;
				}
				return 0;
			}
    		
    	});
    	return tasks;
    }
	
	private List<Fluent> getRoots(Graph<Fluent, FluentConstraint> graph) {
		List<Fluent> roots = new ArrayList<Fluent>();
		for (Fluent f : graph.getVertices()) {
			boolean hasPredecessor = false;
			for (FluentConstraint fc : graph.getInEdges(f)) {
				if (fc.getType() == FluentConstraint.Type.DC) {
					hasPredecessor = true;
					break;
				}
			}
			if (!hasPredecessor) {
				roots.add(f);
			}
		}
		return roots;
	}
	
    private int calculateDimensionX(Fluent f) {

        int size = 0;
        for (FluentConstraint fc : graph.getOutEdges(f)) {
        	if (fc.getType() == FluentConstraint.Type.DC) {
        		size += calculateDimensionX((Fluent) fc.getTo()) + distX;
        	}
        }

//        int min = f.getName().length() * 5;
        int min = 0;
        size = Math.max(min, size - distX);
        basePositions.put(f, size);

        return size;
    }
    
    protected void setCurrentPositionFor(Fluent vertex) {
    	int x = m_currentPoint.x;
    	int y = m_currentPoint.y;
    	if(x < 0) size.width -= x;
    	
    	if(x > size.width-distX) 
    		size.width = x + distX;
    	
    	if(y < 0) size.height -= y;
    	if(y > size.height-distY) 
    		size.height = y + distY;
    	locations.get(vertex).setLocation(m_currentPoint);
    }

	@Override
	public Point2D transform(Fluent input) {
		return locations.get(input);
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInitializer(Transformer<Fluent, Point2D> initializer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGraph(Graph<Fluent, FluentConstraint> graph) {
		this.graph = graph;
		buildTree();
		
	}

	@Override
	public Graph<Fluent, FluentConstraint> getGraph() {
		return graph;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSize(Dimension d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public void lock(Fluent v, boolean state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLocked(Fluent v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLocation(Fluent v, Point2D location) {
		locations.get(v).setLocation(location);
		
	}

}
