/**
 * 
 */
package movement;

import java.util.List;

import movement.map.MapNode;
import core.Coord;
import core.Settings;

/**
 * @author Yang Wenjing
 *
 */
public class STARTMovement extends ShortestPathMapBasedMovement {

	/**
	 * @param settings
	 */
	public STARTMovement(Settings settings) {
		super(settings);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param mbm
	 */
	public STARTMovement(ShortestPathMapBasedMovement mbm) {
		super(mbm);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Path getPath() {
		Path p = new Path(generateSpeed());
		/**
		 * TODO:������ʵ��
		 * 1.�ҵ�Ŀ�Ľڵ�
		 * 2.��ȡpath
		 * 3.��path����
		 */
		MapNode to = selectDestination();
		
		List<MapNode> nodePath = getPathFinder().getShortestPath(lastMapNode, to);
		
		// this assertion should never fire if the map is checked in read phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
			to + ". The simulation map isn't fully connected";
				
		for (MapNode node : nodePath) { // create a Path from the shortest path
			p.addWaypoint(node.getLocation());
		}
		
		lastMapNode = to;
		
		return p;
	}	
	
	@Override
	public Coord getInitialLocation() {
		/**
		 * ��ʼ���ڵ�λ��
		 * ��DTNHost�б�����
		 */
		List<MapNode> nodes = getMap().getNodes();
		MapNode n,n2;
		Coord n2Location, nLocation, placement;
		double dx, dy;
		double rnd = rng.nextDouble();
		
		// choose a random node (from OK types if such are defined)
		//do {
			n = nodes.get(rng.nextInt(nodes.size()));
		//} while (okMapNodeTypes != null && !n.isType(okMapNodeTypes));
		
		// choose a random neighbor of the selected node
		n2 = n.getNeighbors().get(rng.nextInt(n.getNeighbors().size())); 
		
		nLocation = n.getLocation();
		n2Location = n2.getLocation();
		
		placement = n.getLocation().clone();
		
		dx = rnd * (n2Location.getX() - nLocation.getX());
		dy = rnd * (n2Location.getY() - nLocation.getY());
		
		placement.translate(dx, dy); // move coord from n towards n2
		
		this.lastMapNode = n;
		return placement;
	}
	
	/**
	 * �����е�lastMapNode�ҵ�Ŀ�Ľڵ�
	 * @return Ŀ��MapNode�ڵ�
	 */
	public MapNode selectDestination()
	{
		return null;
		
	}

}
