/**
 * 
 */
package movement;

import java.util.List;
import java.util.Random;

import movement.map.MapNode;
import movement.map.SimMap;
import core.Coord;
import core.Settings;
import core.SimClock;

/**
 * @author Yang Wenjing
 * �� STARTMovement �ļ򻯰汾
 * 1.��������0,1״̬��
 * 2.����ת�Ƹ���ֻ����״̬�仯��λ��
 * 3.�ٶȽ�0,1ͳһ����
 */
public class S_STARTMovement extends ShortestPathMapBasedMovement {
	/** ���ֳ���״̬ */
	private int status;
	
	/** �ж��Ƿ񳬹�����ʱ�� */
	private int timer;
	
	/** ��¼�ڵ���ٶ� */
	private double speed;
	/** ��¼�ڵ�ĳ���ʱ�� */
	private double duration;
	
	/** ״̬0  ���ó���ʱ���Ĳ��� */
	private static double DURATION_A_FOR_STATUS0 = 0.971101;
	private static double DURATION_PARA_FOR_STATUS0 = 0.00217593;
	
	/** ״̬1 �ĳ���ʱ������*/
	private static double DURATION_A_FOR_STATUS1 = 0.988955;
	private static double DURATION_PARA_FOR_STATUS1 = 0.00103644;
		
	private static EventAwareRegions[] event_regions=null;
	
	public static final String TRANSITION_PROB_0 = "TransProbFile0";
	public static final String TRANSITION_PROB_1 = "TransProbFile1";
	public static final String CELLS_0 = "Cell0";
	public static final String CELLS_1 = "Cell1";

	/**
	 * @param settings
	 */
	public S_STARTMovement(Settings settings) {
		super(settings);
		// TODO Auto-generated constructor stub
		this.status = rng.nextInt(2);
		
		EventAwareRegions.map = getMap();
		initEventRegions(settings);
	}
	
	public static void initEventRegions(Settings settings)
	{
		if(event_regions!=null)return;
		System.out.println("��ʼ����������");
		
		event_regions = new EventAwareRegions[2];
		event_regions[0] = new EventAwareRegions(0,settings.getSetting(CELLS_0),
				settings.getSetting(TRANSITION_PROB_0));
		event_regions[1] = new EventAwareRegions(1,settings.getSetting(CELLS_1),
				settings.getSetting(TRANSITION_PROB_1));
	}

	/**
	 * @param mbm
	 */
	public S_STARTMovement(STARTMovement mbm) {
		super(mbm);
		// TODO Auto-generated constructor stub
	}
	
	private int reverseStatus(int status)
	{
		return status==1?0:1;
	}
	
	/**
	 * ������ʵ��
	 * 1.�ҵ�Ŀ�Ľڵ�
	 * 2.��ȡpath
	 * 3.��path����
	 */
	@Override
	public Path getPath() {
		this.speed = generateSpeed(this.status);
		Path p = new Path(speed);

		this.setTimer();
		Cell c = event_regions[this.status].fromMN2Cell(this.lastMapNode);
		MapNode to = event_regions[reverseStatus(this.status)].findMapNodeInDis(this.lastMapNode.getLocation(),
				c.region_id,
				this.speed*this.duration);
		List<MapNode> nodePath = getPathFinder().getShortestPath(lastMapNode, to);
		
		// this assertion should never fire if the map is checked in read phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
			to + ". The simulation map isn't fully connected";
				
		for (MapNode node : nodePath) { // create a Path from the shortest path
			p.addWaypoint(node.getLocation());
		}
		
		lastMapNode = to;
		this.status=this.status==0?1:0;//�ı䳵��״̬��
		return p;
	}	
	
	
	/**
	 * ��ʼ���ڵ�λ��
	 * ��DTNHost�б�����
	 */
	@Override
	public Coord getInitialLocation() {

		MapNode node = this.event_regions[this.status].getInitMapNode();
		this.lastMapNode = node;
		return this.lastMapNode.getLocation();
	}
	
	private void setTimer() {
		this.duration = generateLastingTime(this.status);
		this.timer = SimClock.getIntTime()+(int)this.duration;
		
	}
	private double generateLastingTime(int status)
	{
		double seed =  Math.random();
		return seed*10800;
	}
	
	/**
	 * �����ٶ�
	 */
	protected double generateSpeed(double status)
	{
		// TODO get speed by the status
		if(status==0)
			return generateSpeedForStatus0();
		else
			return generateSpeedForStatus1();
			
	}

	private double generateSpeedForStatus0() {
		double  prob = Math.random();
		while(prob>cumulativeSpeedDistributionForStatus0(120))
		{
			prob = Math.random();
		}
		int speed = 0; 
		while(prob>cumulativeSpeedDistributionForStatus0(speed))
		{
			speed++;
		}

		return speed/3.6;
	}
	
	private double generateSpeedForStatus1() {

		double  prob = Math.random();
		while(prob>cumulativeSpeedDistributionForStatus1(120))
		{
			prob = Math.random();
		}
		int speed = 0; 
		while(prob>cumulativeSpeedDistributionForStatus1(speed))
		{
			speed++;
		}

		return speed/3.6;
	}
	/**
	 * g(x) = 0.00792899*x+0.534121
f(x) = 1-exp( -0.0644977*x+0.826339)
	 * @param v
	 * @return
	 */
	private double cumulativeSpeedDistributionForStatus0(int v)
	{
		if(v<0)return 0.0;
		if(v<=40) return 0.00792899*v+0.534121;
		if(v<=120) return 1.0-Math.exp(-0.0644977*v+0.826339);		
		return 1.0;
	}
	
	private double cumulativeSpeedDistributionForStatus1(int v)
	{
		if(v<0) return 0;
		if(v<=40) return 0.00792899*v+0.534121;
		if(v<=120) return 1.0-Math.exp(-0.0644977*v+0.826339);
		return 1.0;
	}
}
