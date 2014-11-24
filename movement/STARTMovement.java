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
 *
 */
public class STARTMovement extends ShortestPathMapBasedMovement {
	
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
	public STARTMovement(Settings settings) {
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
	public STARTMovement(STARTMovement mbm) {
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
		
		if(speed==0)
		{
			System.out.println("�ٶ�Ϊ0�����");
			p.addWaypoint(this.lastMapNode.getLocation());
			this.status=this.status==0?1:0;//�ı䳵��״̬��
			this.setTimer();
			return p;
		}
		//���õȴ�ʱ��
		if(SimClock.getIntTime()<this.timer)
		{
			System.out.println("����ʱ��û��������");
			p.addWaypoint(this.lastMapNode.getLocation());
			p.setSpeed(0);
			return p;
		}

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
		double seed = Math.random();
		if(status==0)
		{
			while(seed>cumulativeLastingTimeForStatus0(3600))
			{	
				seed = Math.random();
			}
			return generateLastingTimeForStatus0(seed);
		}
		else
		{
			while(seed>cumulativeLastingTimeForStatus1(3600))
			{	
				seed = Math.random();
			}
			return generateLastingTimeForStatus1(seed);
		}
	}
	private double generateLastingTimeForStatus1(double seed)
	{
		int maxLength = 3600;
		int tmpLen_bak_max = maxLength;
		int tmpLen_bak_min = 0;
		int tmpLen = maxLength/2;
		if(seed>=cumulativeLastingTimeForStatus1(maxLength))return maxLength;
		if(seed<=cumulativeLastingTimeForStatus1(0)) return 0;
		
		
		do{
			if(seed<cumulativeLastingTimeForStatus1(tmpLen))
			{
				tmpLen_bak_max = tmpLen;
				tmpLen = (tmpLen_bak_max-tmpLen_bak_min)/2+tmpLen_bak_min;
			}
			else if(seed>cumulativeLastingTimeForStatus1(tmpLen))
			{
				tmpLen_bak_min = tmpLen;
				tmpLen = (tmpLen_bak_max-tmpLen_bak_min)/2+tmpLen_bak_min;
			}
			else
				return tmpLen;
				
		}
		while(Math.abs(tmpLen_bak_max-tmpLen_bak_min)<=1);
			
		return tmpLen;
		
		
	}
	
	
	private double generateLastingTimeForStatus0(double seed)
	{
		int maxLength = 3600;
		int tmpLen_bak_max = maxLength;
		int tmpLen_bak_min = 0;
		int tmpLen = maxLength/2;
		if(seed>=cumulativeLastingTimeForStatus0(maxLength))return maxLength;
		if(seed<=cumulativeLastingTimeForStatus0(0)) return 0;
		
		//System.out.println("calculate lasting time...");
		do{
			if(seed<cumulativeLastingTimeForStatus0(tmpLen))
			{
				tmpLen_bak_max = tmpLen;
				tmpLen = (tmpLen_bak_max-tmpLen_bak_min)/2+tmpLen_bak_min;
			}
			else if(seed>cumulativeLastingTimeForStatus0(tmpLen))
			{
				tmpLen_bak_min = tmpLen;
				tmpLen = (tmpLen_bak_max-tmpLen_bak_min)/2+tmpLen_bak_min;
			}
			else
				return tmpLen;
				
		}
		while(Math.abs(tmpLen_bak_max-tmpLen_bak_min)<=100);
		//System.out.println("finish calculating lasting time...");
		return tmpLen;
		
		
	}
	
	private double cumulativeLastingTimeForStatus0(int timeLength)
	{
		if(timeLength<0) return 0;
		return DURATION_A_FOR_STATUS0-Math.exp(-DURATION_PARA_FOR_STATUS0*timeLength);
		
	}
	private double cumulativeLastingTimeForStatus1(int timeLength)
	{
		if(timeLength<0) return 0;
		return DURATION_A_FOR_STATUS1-Math.exp(-DURATION_PARA_FOR_STATUS1*timeLength);
		
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

		return (double)speed/3.6;
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

		return (double)speed/3.6;
	}
	
	private double cumulativeSpeedDistributionForStatus0(int v)
	{
		if(v==0)return 0.660763;
		if(v<=40) return 0.0059774*v+0.660763;
		if(v<=120) return 1.0-Math.exp(-0.0644895*v+0.383622);		
		return 1.0;
	}
	
	private double cumulativeSpeedDistributionForStatus1(int v)
	{
		if(v==0)return 0.217714;
		if(v<=40) return 0.0127845*v+0.217714;
		if(v<=120) return 1.0-Math.exp(-0.0642494*v+1.45314);
		return 1.0;
	}
}
