package mas;



import jade.core.Agent;
import jade.core.Location;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;


import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.util.leap.Iterator;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agents.AgentAventurier;
import agents.AgentExplorer;
import agents.AgentReussi;
import env.Environment;
import env.Environment.ENVtype;



/**
 * The Jade's documentation is bad, you can hardly find how to launch a platform from source code.  
 * I hope these few classes will be useful.
 * 
 * Here:  1 main container, 3 containers, 4 agents
 * Agent0 on container0
 * Agent1 and Agent3 on container1
 * Agent2 on container2
 * 
 * Agent0 will send a message to agent1 that's all :)
 * 
 * The "pause" when launching the platform  is just created to let you the possibility to activate the sniffer agent from its GUI in order to sniff the agents and to graphicaly see the message passing.
 * In any case, I chose here to print the sent/received on the standard output.
 * 
 * For sake of simplicity, the creation of the containers and of the agents is hardcoded, and I do not activate the sniffer.  It is bad.
 * 
 * 
 * Tested with Jade 3.7, Jade 4.0 and 4.3.3
 * 
 * 
 * @author Cedric Herpson
 *
 */

public class EmptyEnv {

	private static String hostname = "127.0.0.1"; 
	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref
	private static Runtime rt;	

	private static Environment env;// static ref of the real environment

	public static void main(String[] args){
		
		
		
		//0) Create the real environment and the observed one


				/////
				////
				////

				env= new Environment(ENVtype.GRID , 5); // ANCIENEMENT (true,30)
				////
				////
				///

				//1), create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
				rt=emptyPlatform(containerList);

				//2) create agents and add them to the platform.
				agentList=createAgents(containerList);

				//		try {
				//			System.out.println("Press a key to start the agents");
				//			System.in.read();
				//		} catch (IOException e) {
				//			// TODO Auto-generated catch block
				//			e.printStackTrace();
				//		}

				//3) launch agents
				startAgents(agentList);


		//0) Create the real environment and the observed one

/**
		/////
		////
		////
		int port = 8888;
		String cied = "PPTI-14-407-11";
		String ciedName = " Hell";
		String prof = "ppti-14-407-14";
		String profName = "idPlatform";
		String myName = "le mec-a-coté-de-la-porte";
		
		Profile p = new ProfileImpl(prof, port , prof);
		p.setParameter("container-name", myName);
		ContainerController cc = Runtime.instance().createAgentContainer(p);
		
		
		
		
		// creation de conteneur (local)
		
				//env= new Environment(ENVtype.GRID , 2); // ANCIENEMENT (true,30)
				//rt=emptyPlatform(containerList);
		
				
//				String containerName;
//				ProfileImpl pContainer;
//				ContainerController containerRef;
//				HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.
//
//
//				System.out.println("Launching containers ...");
//
//				//create the container0	
//				containerName="container0";
//				pContainer = new ProfileImpl(null, 8888, null);
//				System.out.println("Launching container "+pContainer);
//				containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
//				containerList.put(containerName, containerRef);
//		
//		
//				
//
//				System.out.println("Launching agents...");
//				ContainerController c;
//				String agentName;
//				List<AgentController> agentList=new ArrayList();
//				
		
		//Agent1 on container0
//				c = containerList.get("container0");
				String agentName="Indiana Jones";
				try {
				//	Object[] objtab=new Object[]{env};//used to give informations to the agent
					AgentController	ag2=cc.createNewAgent(agentName,AgentReussi.class.getName(),null);
					//agentList.add(ag2);
					System.out.println(agentName+" launched");
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	**
		
		
		
		
		
		// creation de conteneur (local)
		
		env= new Environment(ENVtype.GRID , 2); // ANCIENEMENT (true,30)
		rt=emptyPlatform(containerList);
		
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.


		System.out.println("Launching containers ...");

		//create the container0	
		containerName="container0";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		// creation d un agent (en local)
		
		
		System.out.println("Launching agents...");
		ContainerController c;
		String agentName;
		List<AgentController> agentList=new ArrayList();
		
		
		//Agent1 on container0
		c = containerList.get("container0");
		agentName="Indiana Jones";
		try {
			Object[] objtab=new Object[]{env};//used to give informations to the agent
			AgentController	ag2=c.createNewAgent(agentName,AgentReussi.class.getName(),objtab);
			agentList.add(ag2);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//start de l'agent

		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
	

		env= new Environment(ENVtype.GRID , 2); // ANCIENEMENT (true,30)
		////
		////
		///

		//1), create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
		rt=emptyPlatform(containerList);

		//2) create agents and add them to the platform.
		agentList=createAgents(containerList);

		//		try {
		//			System.out.println("Press a key to start the agents");
		//			System.in.read();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		//3) launch agents
		startAgents(agentList);
		
		
		
//		QueryAgentsOnLocation queryLocation = new QueryAgentsOnLocation();
//		queryLocation.setLocation(agentList.get(1)); 
	
**/
	}
		


	/**********************************************
	 * 
	 * Methods used to create an empty platform
	 * 
	 **********************************************/

	/**
	 * Create an empty platform composed of 1 main container and 3 containers.
	 * 
	 * @return a ref to the platform and update the containerList
	 */
	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList){

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(hostname, 8888, "plateform");
		System.out.println("Launching a main-container..."+pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		// 2) create the containers
		containerList.putAll(createContainers(rt));

		// 3) create monitoring agents : rma agent, used to debug and monitor the platform; sniffer agent, to monitor communications; 
		createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return rt;

	}

	/**
	 * Create the containers used to hold the agents 
	 * @param rt The reference to the main container
	 * @return an Hmap associating the name of a container and its object reference.
	 * 
	 * note: there is a smarter way to find a container with its name, but we go fast to the goal here. Cf jade's doc.
	 */
	private static HashMap<String,ContainerController> createContainers(Runtime rt) {
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.


		System.out.println("Launching containers ...");

		//create the container0	
		containerName="container0";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		//create the container1	
		containerName="container1";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		//create the container2	
		containerName="container2";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		System.out.println("Launching containers done");
		return containerList;
	}


	/**
	 * create the monitoring agents (rma+sniffer) on the main-container given in parameter and launch them.
	 *  - RMA agent's is used to debug and monitor the platform;
	 *  - Sniffer agent is used to monitor communications
	 * @param mc the main-container's reference
	 * @return a ref to the sniffeur agent
	 */
	private static void createMonitoringAgents(ContainerController mc) {

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma;

		try {
			rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}

		System.out.println("Launching  Sniffer agent on the main container...");
		AgentController snif=null;

		try {
			snif= mc.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer",new Object[0]);
			snif.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("launching of sniffer agent failed");

		}		


	}



	/**********************************************
	 * 
	 * Methods used to create the agents and to start them
	 * 
	 **********************************************/


	/**
	 *  Creates the agents and add them to the agentList.  agents are NOT started.
	 *@param containerList :Name and container's ref
	 *@param sniff : a ref to the sniffeur agent
	 *@return the agentList
	 */
	private static List<AgentController> createAgents(HashMap<String, ContainerController> containerList) {
		System.out.println("Launching agents...");
		ContainerController c;
		String agentName;
		List<AgentController> agentList=new ArrayList();

		//wumpus on container0
		c = containerList.get("container0");
		agentName="wumpus";
		try {


			Object[] objtab=new Object[]{env};//used to give informations to the agent
			AgentController	ag=c.createNewAgent("wumpus",DummyAgentWumpus.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Agent0 on container0
		c = containerList.get("container0");
		agentName="la magicienne";
		try {


			Object[] objtab=new Object[]{env};//used to give informations to the agent
			AgentController	ag=c.createNewAgent(agentName,AgentAventurier.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Agent1 on container0
		c = containerList.get("container0");
		agentName="le ranger";
		try {


			Object[] objtab=new Object[]{env};//used to give informations to the agent
			AgentController	ag2=c.createNewAgent(agentName,AgentAventurier.class.getName(),objtab);
			agentList.add(ag2);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		System.out.println("Agents launched...");
		return agentList;
	}

	/**
	 * Start the agents
	 * @param agentList
	 */
	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");


		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}

}







