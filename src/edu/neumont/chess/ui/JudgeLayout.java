package edu.neumont.chess.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.HashMap;

public class JudgeLayout implements LayoutManager2 {
	private HashMap<Component, GridBagConstraints> constraints;
	
	public JudgeLayout() {
		constraints = new HashMap<Component, GridBagConstraints>();
	}
	
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
 		if( constraints instanceof GridBagConstraints ) {
 			Object copy = ((GridBagConstraints)constraints).clone();
			this.constraints.put(comp, (GridBagConstraints)copy);
 		}
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		Component[][] grid = getGrid(target);
		if( grid.length == 0 || grid[0].length == 0 )
			return new Dimension(0,0);
		
		double[] rowHeights = new double[grid.length];
		double[] columnWidths = new double[grid[0].length];
		for( int c=0; c < grid[0].length; c++ )
			columnWidths[c] = 0;
		for( int r=0; r < grid.length; r++ ) {
			rowHeights[r] = 0;
			for( int c=0; c < grid[r].length; c++ ) {
				Dimension dim = grid[r][c] != null ? grid[r][c].getMaximumSize() : null;
				if( dim != null ) {
					if( dim.getWidth() > columnWidths[c] )
						columnWidths[c] = dim.getWidth();
					if( dim.getHeight() > rowHeights[r] )
						rowHeights[r] = dim.getHeight();
				}
			}
		}

		double width = 0;
		double height = 0;
		for( int r=0; r < rowHeights.length; r++ )
			height += rowHeights[r];
		for( int c=0; c < columnWidths.length; c++ )
			width += columnWidths[c];
		
		return new Dimension((int)(width+0.5), (int)(height+0.5));
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		Component[][] grid = getGrid(parent);
		if( grid.length == 0 || grid[0].length == 0 )
			return;

		// Setup the top and left sides of the grid
		double topHeight = 0;
		double leftWidth = 0;
		int width = parent.getWidth();
		int height = parent.getHeight();
		for( int r=0; r < grid.length; r++ ) {
			double w = grid[r][0] == null ? 0 : grid[r][0].getPreferredSize().getWidth();
			leftWidth = Math.max(leftWidth, w);
		}
		for( int c=0; c < grid[0].length; c++ ) {
			double h = grid[0][c] == null ? 0 : grid[0][c].getPreferredSize().getHeight();
			topHeight = Math.max(topHeight, h);
		}
		

		double cellWidth = 0;
		double cellHeight = 0;
		if( grid[0].length > 1 )
			cellWidth = (width-leftWidth) / (grid[0].length-1);
		if( grid.length > 1 )
			cellHeight = (height-topHeight) / (grid.length-1);
		
		for( int r=0; r < grid.length; r++ ) {
			if( grid[r][0] != null ) {
				double y = r==0 ? 0 : topHeight + (r-1)*cellHeight;
				Rectangle bounds = new Rectangle( 0, (int)Math.ceil(y), (int)Math.ceil(leftWidth),
						(int)Math.ceil(r==0?topHeight:cellHeight) );
				grid[r][0].setBounds( bounds );
			}
		}
		for( int c=0; c < grid[0].length; c++ ) {
			if( grid[0][c] != null ) {
				double x = c==0 ? 0 : leftWidth + (c-1)*cellWidth;
				Rectangle bounds = new Rectangle((int)Math.ceil(x), 0,
						(int)Math.ceil(c==0 ? leftWidth : cellWidth), (int)Math.ceil(topHeight));
				grid[0][c].setBounds(bounds);
			}
		}
		
		
		// Now lay out the rest of the grid
		for( int r=1; r < grid.length; r++ ) {
			for( int c=1; c < grid[r].length; c++ ) {
				if( grid[r][c] != null ) {
					int x = (int)Math.ceil(leftWidth + (c-1)*cellWidth);
					int y = (int)Math.ceil(topHeight + (r-1)*cellHeight);
					int w = (int)Math.ceil(cellWidth);
					int h = (int)Math.ceil(cellHeight);
					grid[r][c].setBounds(x, y, w, h);
				}
			}
		}
	}
	
	private Component[][] getGrid( Container parent ) {
		Component[] components = parent.getComponents();
		int minRow = Integer.MAX_VALUE;
		int maxRow = 0;
		int minCol = Integer.MAX_VALUE;
		int maxCol = 0;
		boolean hasComponents = false;
		for( Component comp : components ) {
			if( this.constraints.containsKey(comp) ) {
				GridBagConstraints constraints = this.constraints.get(comp);
				if( constraints.gridy > maxRow )
					maxRow = constraints.gridy;
				if( constraints.gridy < minRow )
					minRow = constraints.gridy;
				if( constraints.gridx > maxCol )
					maxCol = constraints.gridx;
				if( constraints.gridx < minCol )
					minCol = constraints.gridx;
				hasComponents = true;
			}
		}
		
		if( !hasComponents ) {
			return new Component[0][0];
		}
		
		Component[][] grid = new Component[maxRow-minRow+1][maxCol-minCol+1];
		for( Component comp : components ) {
			if( this.constraints.containsKey(comp) ) {
				GridBagConstraints constraints = this.constraints.get(comp);
				grid[constraints.gridy-minRow][constraints.gridx-minCol] = comp;
			}
		}
		
		return grid;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		Component[][] grid = getGrid(parent);
		if( grid.length == 0 || grid[0].length == 0 )
			return new Dimension(0,0);
		
		double[] rowHeights = new double[grid.length];
		double[] columnWidths = new double[grid[0].length];
		for( int c=0; c < grid[0].length; c++ )
			columnWidths[c] = 0;
		for( int r=0; r < grid.length; r++ ) {
			rowHeights[r] = 0;
			for( int c=0; c < grid[r].length; c++ ) {
				Dimension dim = grid[r][c] != null ? grid[r][c].getMinimumSize() : null;
				if( dim != null ) {
					if( dim.getWidth() > columnWidths[c] )
						columnWidths[c] = dim.getWidth();
					if( dim.getHeight() > rowHeights[r] )
						rowHeights[r] = dim.getHeight();
				}
			}
		}

		double width = 0;
		double height = 0;
		for( int r=0; r < rowHeights.length; r++ )
			height += rowHeights[r];
		for( int c=0; c < columnWidths.length; c++ )
			width += columnWidths[c];
		
		return new Dimension((int)(width+0.5), (int)(height+0.5));
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		Component[][] grid = getGrid(parent);
		if( grid.length == 0 || grid[0].length == 0 )
			return new Dimension(0,0);
		
		double[] rowHeights = new double[grid.length];
		double[] columnWidths = new double[grid[0].length];
		for( int c=0; c < grid[0].length; c++ )
			columnWidths[c] = 0;
		for( int r=0; r < grid.length; r++ ) {
			rowHeights[r] = 0;
			for( int c=0; c < grid[r].length; c++ ) {
				Dimension dim = grid[r][c] != null ? grid[r][c].getPreferredSize() : null;
				if( dim != null ) {
					if( dim.getWidth() > columnWidths[c] )
						columnWidths[c] = dim.getWidth();
					if( dim.getHeight() > rowHeights[r] )
						rowHeights[r] = dim.getHeight();
				}
			}
		}

		double width = 0;
		double height = 0;
		for( int r=0; r < rowHeights.length; r++ )
			height += rowHeights[r];
		for( int c=0; c < columnWidths.length; c++ )
			width += columnWidths[c];
		
		return new Dimension((int)(width+0.5), (int)(height+0.5));
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		constraints.remove(comp);
	}
}
