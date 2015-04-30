package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.CaseInsensitiveString;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;






import chemin.Dijkstra;
import chemin.Graphe;
import env.Attribute;
import env.Environment;
import env.Environment.Couple;
import mas.abstractAgent;

public class AgentAventurier extends abstractAgent{

	private static String defaultNode = " node { fill-color: white;text-color: black; }";
	private static String wind = "node_wind {fill-color: white;text-color:pink;}";
	private static String well = "node_well {fill-color: white;text-color:blue;}";
	private static String treasure = "node_treasure {fill-color: white;text-color:orange;}";
	private static String Nodes = defaultNode + wind + well + treasure ;

	//Gere le labyrhynte . Le premier string le noeuf pere , le deuxieme string , les noeud fixe, le integer si l'arrete entre pere-fils est visité ou pas
	protected HashMap<String , HashMap<String,Integer>> maze ;

	// si l'agent est bloquer dans son exploration du graphe
	protected boolean stuck = false ;

	// la liste des agents qui l'ont déja contacter
	private ArrayList<AID> senders ;

	// sa position actuelle
	private String pos="";

	// sa prochaine destination
	private String dest;

	// une liste des parents de chaque noeuds
	protected HashMap<String,String> parentNoeud ;

	//détermine dans quel état son les neoud (puis , wumpus, rien)
	protected HashMap <String, StateMaze> etatNoeud;

	// la liste des différents bihavior qu'a l'agent
	protected HashMap<String,Behaviour> comportement ;
	
	
	protected ArrayList<ACLMessage> boiteEnvoie;

	//le temps entre chaque pas
	int clock = 1000;


	//Graph graph ;

	protected void setup(){
		super.setup();

		// initialisse les différents atribut
		maze = new HashMap<String , HashMap<String,Integer>>();
		senders = new ArrayList<AID>();
		parentNoeud = new  HashMap<String,String>();
		etatNoeud = new HashMap<String, StateMaze>();
		comportement = new HashMap<String, Behaviour>();
		//	graph = new SingleGraph(getLocalName());
		//	graph.addAttribute("ui.stylesheet", Nodes);
		//	graph.display();
		boiteEnvoie= new ArrayList<ACLMessage>();

		//get the parameters given into the object[]
		final Object[] args = getArguments();
		if(args[0]!=null){
			realEnv = (Environment) args[0];
			realEnv.deployAgent(this.getLocalName());

		}else{
			System.out.println("Erreur lors du tranfert des parametres");
		}

		//Add the behaviours
		comportement.put("listen", new ListenerBehaviour(this));
		comportement.put("explore", new ExplorerBehaviour(this,realEnv));
		addBehaviour(comportement.get("explore"));
		addBehaviour(comportement.get("listen"));

		System.out.println("the agent "+this.getLocalName()+ " is started");


		// Ajout au pages jaune.
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID()); /* getAID est l'AID de l'agent qui veut s'enregistrer*/
		ServiceDescription sd  = new ServiceDescription();
		sd.setType( "AventurierDeLextreme" ); /* il faut donner des noms aux services qu'on propose (ici explorer)*/
		sd.setName(getName() );
		dfd.addServices(sd);

		try {  
			DFService.register(this, dfd ); 
			AMSService.deregister(this);
		}
		catch (FIPAException fe) { fe.printStackTrace(); }


	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		System.out.println(this.getLocalName()+ " est mort en " + pos);
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void afficherGraphe(){
		String s ="";
		for (String s1 : maze.keySet()){
			s+= s1 +"={";
			for (String s2 : maze.get(s1).keySet()){
				s+= s2 +" ; ";
			}
			s+= "} \n";
		}
		System.out.println(s);
	}


	// sert a transformer l'attribut maze en matrice d'adjacence pour l'utiliser avec disktra (la classe)
	public int[][] matriceAdjacente(ArrayList<String> noeud){
		int size = maze.size();
		int[][] result = new int [size][size];
		//ArrayList<String> noeud = new ArrayList<String>();

		for (int i = 0 ; i < size ; i++){
			for (int j = 0 ; j < size ; j++){
				if (maze.get(noeud.get(i)).containsKey(noeud.get(j))){
					if(etatNoeud.get(noeud.get(i)) == StateMaze.Puits || etatNoeud.get(noeud.get(j)) == StateMaze.Puits){
						result[i][j]= 100000;
					}
					result[i][j]= 1;
				}
				else {
					result[i][j]= -999;
				}
			}
		}
		return result ;
	}


	// utilise dijkstra (la classe)
	// Donne le chemin entre le chemin ou on est et la destination donn� en param
	public ArrayList<String> plusCourtChemin(String destination){
		ArrayList<String> noeud = new ArrayList<String>();
		for (String s : maze.keySet()){
			noeud.add(s);
		}
		if (noeud.contains(destination) == false){
			System.out.println(getLocalName()+" : destination non reconnue  " );
			ArrayList<String> vide = new ArrayList<String>();
			return vide;
		}
		int [][] ma = matriceAdjacente(noeud);
		Graphe g = new Graphe(ma);

		ArrayList<String> result = new ArrayList<String>();
		// Utilise la classe Disjktra pour calculer pour cours chemin , si chemi  n'exsite pas , cr�e une exception
		try {
			Dijkstra dj = new Dijkstra(noeud.indexOf(getCurrentPosition()), g);
			Vector<Integer> v = dj.afficheChemin(noeud.indexOf(destination));

			//System.out.println("noeud = " +noeud);
			for ( int i = v.size() -1; i >=0 ;i--){
				result.add(noeud.get(v.get(i)));
			}
			result.add (destination);
			//System.out.println(getLocalName()+" plus court chemin done !");
		}
		// si chemin n'existe pas , on renvoie la position
		catch(Exception e){
			System.out.println("Probleme de dijkstra : pas de chemin trouve pour "+ getLocalName());
			result.add(getCurrentPosition());
		}
		return result ;
	}


	protected void afficherMaze(){
		int taille = Math.min(etatNoeud.size(), 5);
		String affiche = "";
		for (int i =0 ; i< taille ; i++){
			for (int j=0 ; j < taille;j++){
				String s = i+ "_"+j ;
				if (maze.containsKey(s)){
					//affiche += " "+s;
					if (this.etatNoeud.containsKey(s)){
						StateMaze state= etatNoeud.get(s);
						switch(state){
						case Inconnue :
							affiche +=" "+i+"I"+j;
							break;
						case Puits :
							affiche +=" "+i+"%"+j;
							break;
						case Visiter :
							affiche +=" "+i+"X"+j;
							break;
						case Rien:
							affiche +=" "+i+"R"+j;
							break;
						default :
							affiche +=" "+s;
							break;
						}
					}
					else {
						affiche +=" "+s;
					}
				}
				else {
					affiche += " ___";
				}
			}
			affiche +="\n";
		}
		System.out.println(affiche);
		
	}

	protected void afficherTypeNoeud(){
		int taille = Math.min(etatNoeud.size(), 5);
		String affiche = "";
		String affiche1 = "";
		String affiche2 = "";
		for (int i =0 ; i< taille ; i++){
			affiche1 = "";
			affiche2 = "";
			for (int j=0 ; j < taille;j++){
				String s = i+ "_"+j ;
				affiche2 += s+" ";
				if (this.etatNoeud.containsKey(s)){
					StateMaze state= etatNoeud.get(s);
					switch(state){
					case Inconnue :
						affiche1 +="\t ?";
						break;
					case Puits :
						affiche1 +="\t %";
						break;
					case Visiter :
						affiche1 +="\t X";
						break;
					case Rien:
						affiche1 +="\t R";
						break;
					default :
						affiche1 +="\t  ";
						break;
					}
				}
				else {
					affiche +="\t  ";
				}
			}
			affiche+= affiche2 +"\n" + affiche1 +"\n" ;
		}
		System.out.println(affiche);
	}


	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS  1  : THE EXPLORER
	 * 
	 * 
	 *******************************************************/


	public class ExplorerBehaviour extends TickerBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		// si l'agent a finit d'explorer ?
		private boolean finished=false;

		//la variable d'environnement
		private Environment realEnv;

		// la derniere position connu ?
		private String lastPosition;
		private abstractAgent myAgent;

		private int unefoissurdeux = 0;
		private int compteur = 0;

		public ExplorerBehaviour (final abstractAgent myagent,Environment realEnv) {
			// Pour changer la vitesse , c'est ICI
			super(myagent, clock);
			this.realEnv=realEnv;
			stuck = false ;
			this.myAgent = myagent;

////			System.out.println("EXPLORER MODE for " + myagent.getLocalName());


		}

		// Truc a faire dans le béhavior d'exploration
		@Override
		protected void onTick() {
			
			System.out.println("dst"+pos);
			
			
			// envoie les messages pas recu
			if (compteur == 10){
				myAgent.addBehaviour(new TalkmemoryBehaviour(myAgent, realEnv));
				compteur =0;
			}
			else {
				compteur ++ ;
			}

			//affichage
			//afficherGraphe();
			//System.out.println(etatNoeud);
			//System.out.println(parentNoeud);

			// ecoute ses messages
			myAgent.addBehaviour (new ListenerBehaviour (myAgent));
			
		
			
///////			System.out.println(myAgent.getLocalName() +": "+ etatNoeud);


			pos=getCurrentPosition();
			/**** A ACTIONNER ****** if (myPosition!="" && stuck == false){
			 */
			//met a jour la liste des arretes
			majMap();

			if (stuck ==true  && findDestination()!= pos){
				comportement.put("destination", new  littlestWayBehaviour(myAgent, pos, findDestination(), realEnv)  );
				addBehaviour(comportement.get("destination"));
				myAgent.removeBehaviour(this);
				comportement.remove("explore");
				return ;
			}
			
			// affiche le labythinte
////			afficherMaze();
			
			//gestion des tresor
			//si l'agent a assez de place il prend le trésor, sinon il demande aux autres agents.
			// Si personne ne répond , il ira prendre le trésore (comme si il était un agent qui aurait était informer par un autre agent)
			List<Attribute> lattribute= realEnv.observe(pos, this.myAgent.getLocalName()).get(0).getR();
			for(Attribute a:lattribute){
				switch (a) {
				case TREASURE:
					System.out.println("My current backpack capacity is:"+ getBackPackFreeSpace());
					System.out.println("Value of the treasure on the current position: "+a.getValue());
					if (getBackPackFreeSpace()>= (Integer)a.getValue())
					{
						System.out.println("I," +  this.myAgent.getLocalName() + " grabbed the treasure entirely :"+pick());
					}
					else{
						myAgent.addBehaviour(new TalkTreasurBehaviour(myAgent, realEnv, pos,(Integer) a.getValue()));
					}
					break;
				default:
					break;
				}
			}			

			//decide de la position
			dest =choixDestination(pos, realEnv);


			//gere l'interblocage !!!!!
			if (dest == lastPosition ){
				dest =aleatoire(pos, realEnv);
			}
			else{
				lastPosition= dest ;
			}
			// bouge l'agent
			move(pos, dest);

		}

		// l'algo de chemin, correspond a un parcours en longueur  , Se sert de random de temps en temps
		protected String choixDestination (String myPosition, Environment env){
			// fait un parcours en longeur , choisit un noeud fis a visiter
			for (String s : maze.get(myPosition).keySet()){
				if (  maze.get(myPosition).get(s) == 0 && etatNoeud.get(s) != StateMaze.Puits  ){
					if (myPosition != s && parentNoeud.containsKey(s)==false) parentNoeud.put(s, myPosition);
					maze.get(myPosition).put(s, 1);
					maze.get(s).put(myPosition, 1);
					//					System.out.println("pos ="+myPosition +"  dest="+  s +": " + myAgent.getLocalName() );
					// donne sa position a tous les agents

					myAgent.addBehaviour(new TalkBehaviour(myAgent, realEnv, pos, s, etatNoeud.get(pos)));


					return s;
				}
			}
			// si tout noeud fils visiter, retour en arriere
			while (parentNoeud.containsKey(myPosition) && !parentNoeud.get(myPosition).equals( "0_0")){
				//				System.out.println("retour a la source " + parentNoeud.get(myPosition)+": "+ myAgent.getLocalName());
				
				return parentNoeud.get(myPosition);
			}

			// Si retour a la racine , fait n'importe quoi.
			//			System.out.println("i have no idea what i'm doing");

			// Fait un mouvement au hasard. Est limité car s'effectura toujours proche de l'origine
			while (true){
				stuck = true;
				double rand = Math.random(); 
				double value =  (1.0 / (maze.get(myPosition).size()*1.0));
				for (String s : maze.get(myPosition).keySet()){

					if ( rand < value ){
						if (myPosition != s && parentNoeud.containsKey(s)==false) parentNoeud.put(s, myPosition);
						return s ;
					}
					value += (1.0 / (maze.get(myPosition).size()*1.0));
				}
			}
		}

		// fonction qui donne un chemin aleatoire poru uen case
		protected String aleatoire(String myPosition, Environment env){
			// fait un parcours en longeur , choisit un noeud fis a visiter
			System.out.println("aleatoire pour : " + myAgent.getLocalName());
			for (String s : maze.get(myPosition).keySet()){
				if (myPosition != s && parentNoeud.containsKey(s)==false) parentNoeud.put(s, myPosition);
				maze.get(myPosition).put(s, 1);
				maze.get(s).put(myPosition, 1);
				myAgent.addBehaviour(new TalkBehaviour(myAgent, realEnv, pos, s, etatNoeud.get(pos)));
				return s;

			}
			return myPosition;
		}


		//Met a jour le labyrinthe connu de l'agent
		protected void majMap() {
			String myPosition=getCurrentPosition();
			// creer un état pour le noeud
			if (etatNoeud.containsKey(myPosition)== false) etatNoeud.put(dest, StateMaze.Inconnue);
			//Met a jour les chemin de la position actuelle au position ateignable et vice versa
			if (myPosition!=""){
				List<Couple<String,List<Attribute>>> lobs=observe(myPosition);
				majMap1(myPosition,lobs);
				majMap2(lobs,myPosition);
				
				//test
//				for (String dest: maze.get(myPosition).keySet()){
//					 lobs=observe(dest);
//					majMap1(dest,lobs);
//					majMap2(lobs,dest);
//				}
//				
				

				// Met a jour les noeuds 
				//System.out.println("position=" + myPosition  + " name =" + this.myAgent.getLocalName()   );
				if (realEnv.observe(myPosition, this.myAgent.getLocalName()).get(0).getR().isEmpty()){
					majEtat(myPosition, StateMaze.Rien);
				}
				for(env.Attribute a : realEnv.observe(myPosition, this.myAgent.getLocalName()).get(0).getR()){
					majEtat(myPosition, defineEtat(a));
				}

			}
		}


		// Met a jour dans le hasmap les chemins de la position courantes au prosition ateingable
		protected void majMap1(String myPosition,List<Couple<String,List<Attribute>>> lobs)
		{
			for (int i = 0 ; i < lobs.size();i++)
			{
				String dest = lobs.get(i).getL();
				// creer un état pour le noeud
				if (etatNoeud.containsKey(dest)== false)
				{
					etatNoeud.put(dest, StateMaze.Inconnue);
					// si maze ne contient pas myPosition comme neoud entrant
					//			graph.addNode(dest);
					//			Node destNode = graph.getNode(dest);
					//					destNode.addAttribute("ui.label", dest);
					//					destNode.addAttribute("ui.class","node"); 
				}
				if (i==0 && !maze.containsKey(myPosition))
				{
					HashMap<String,Integer> temp = new HashMap<String,Integer>();
					temp.put(dest, 0);
					maze.put(myPosition, temp);
					//graph.addNode(myPosition);

				}
				// si l'arrete myPosition/dest n'existe pas.
				if (!maze.get(myPosition).containsKey(dest) && dest != myPosition)
				{
					//					if (graph.getNode(myPosition)==null)
					//					{
					//						graph.addNode(myPosition);
					//						Node destNode = graph.getNode(myPosition);
					//						destNode.addAttribute("ui.label", myPosition);
					//						destNode.addAttribute("ui.class","node"); 
					//					}
					//					if (graph.getNode(dest)==null)
					//					{
					//						graph.addNode(dest);
					//						Node destNode = graph.getNode(dest);
					//						destNode.addAttribute("ui.label", dest);
					//						destNode.addAttribute("ui.class","node"); 
					//					}
					maze.get(myPosition).put(dest, 0);
					//				if(dest!=myPosition)
					//				graph.addEdge(myPosition+dest, dest, myPosition);

				}
			}
		}


		// met à jour le hashmap les chemins des position ateignable a la position courante.
		protected void majMap2(List<Couple<String,List<Attribute>>> lobs,String dest)
		{
			for (int i = 0 ; i < lobs.size();i++)
			{
				String pos =  lobs.get(i).getL(); 
				if(!maze.containsKey(pos))
				{
					HashMap<String,Integer> temp = new HashMap<String,Integer>();
					temp.put(dest, 0);
					maze.put(pos, temp);
				}
				if(!maze.get(pos).containsKey(dest) && dest != pos)
				{
					maze.get(pos).put(dest, 0);
				}
			}
		}



		// Definie dans quel état on est 
		private StateMaze defineEtat (Attribute etat){
			if (etat == env.Attribute.WIND){
				return StateMaze.Puits;
			}
			return StateMaze.Rien;

		}


		// Met a jour les états des différents noeuds
		protected void majEtat (String myPosition , StateMaze etat){
			etatNoeud.put(myPosition, StateMaze.Visiter);
			for (String s : maze.get(myPosition).keySet()){
				if(etatNoeud.get(s) != StateMaze.Rien && etatNoeud.get(s) != StateMaze.Visiter){
					etatNoeud.put(s, etat);
				}
				for (String s2 : maze.get(s).keySet() ){
					if(etatNoeud.get(s2) != StateMaze.Rien && etatNoeud.get(s2) != StateMaze.Visiter){
						etatNoeud.put(s2, etat);
					}
				}
			}
		}
		
		

		public String findDestination() {
			for( String s : etatNoeud.keySet()){
				if ( etatNoeud.get(s) == StateMaze.Rien && s != null) return s ;
			}
			System.out.println("PAS TROUVER DE CHEMIN");
			return pos ;
		}

	}


		/*******************************************************
		 * 
		 * 
		 * 				BEHAVIOURS 2 :  THE TALKER
		 * 
		 * 
		 ********************************************************/


		public class TalkBehaviour extends SimpleBehaviour{
			/**
			 * When an agent choose to move
			 *  
			 */
			private static final long serialVersionUID = 9088209402507795289L;

			private boolean finished=false;
			private Environment realEnv;

			private StateMaze state;
			private String pos;
			private String dest;
			private abstractAgent myAgent;


			public TalkBehaviour (final abstractAgent myagent,Environment realEnv,String pos,String dest,StateMaze state) {
				super();
				this.realEnv=realEnv;
				this.state = state ;
				this.dest = dest;
				this.pos = pos;
				this.myAgent= myagent;


			}

			// Definie dans quel état on est 
			private StateMaze defineEtat (Attribute etat){
				if (etat == env.Attribute.WIND){
					return StateMaze.Puits;
				}
				return StateMaze.Rien;

			}


			@Override
			public void action() {
				//Create a message in order to send it to the choosen agent


				//récupère les adresses de ses gens
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd  = new ServiceDescription();
				sd.setType( "AventurierDeLextreme" ); /* le même nom de service que celui qu'on a déclaré*/
				dfd.addServices(sd);

				DFAgentDescription[] result;
				try {
					result = DFService.search(this.myAgent, dfd);
					AID a = new AID() ;
					for (int i=0 ; i < result.length; i ++){

						a = result[i].getName();

						if (!a.equals(this.myAgent.getAID()) ){
							final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setSender(this.myAgent.getAID());

	//////////////////						System.out.println(myAgent.getLocalName() +" envoie : "+pos + "@" + dest + "&" + state + " pour " + a.getLocalName());


							msg.addReceiver(a); // hardcoded= bad, must give it with objtab				 


							msg.setContent(pos + "@" + dest + "&" + state);
							this.myAgent.sendMessage(msg);
							
							//
							//ajout de gestion des messages
							boiteEnvoie.add(msg);
							//
							//
							//
						}
					}

				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}




				this.finished=true;
			}



			@Override
			public boolean done() {

				return finished;
			}



		}




	




	/*********************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 3 : THE LISTENER
	 * 
	 * 
	 *********************************************************/




	public class ListenerBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to communicate with others agents in order to reach a precise decision, 
		 * it tries to form a coalition. This behaviour is the first step of the paxos
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;

		long time ;
		//dit 
		boolean tresoreEnVue;
		int size ;
		String destination ;
		abstractAgent myAgent;

		public ListenerBehaviour(final abstractAgent myagent) {

			super(myagent);
			tresoreEnVue = false ;
			size = Integer.MAX_VALUE ;
			this.myAgent = myagent;
		}


		public void action() {
			//1) receive the message


			//verifie si il faut aller chercher un tresor
			gestionTresor();
			
			accuserDeReception();


			final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			final ACLMessage msg = this.myAgent.receive(msgTemplate);

			if (msg != null ) {	
///////////////////////				System.out.println(myAgent.getLocalName()+" a recu "+ msg.getContent());

				//				AID sender = msg.getSender();
				//				if ( ! senders.contains(sender)){
				//					senders.add(sender);
				//				}


				// Lit le message et met a jour le maze
				String message = msg.getContent();

				if (message.contains("Treasure")){
					String[]listeMsg = message.split("@");
					calculTreasure(listeMsg[1], Integer.valueOf(listeMsg[2]));
					destination = listeMsg[1];

				}

				else if (message.contains("Capacity")){
					compareDistance(message);
				}

				else {
					
					envoieAccuser(msg.getSender());

					String[]result= new String[3];
					String[]listeMsg = message.split("@");
					String[]listeMsg2 = listeMsg[1].split("&"); 
					result[0]=listeMsg[0]; 
					result[1]= listeMsg2[0];
					result[2]=listeMsg2[1]; 
					majMap3(result[0],result[1]);


					// Modifie les états comme si l'agent avait était lui même sur la case
					majEtat2(result[0], StateMaze.valueOf( result[2]));


					// Modifie les états en considérant comme sur la case ou a était l'agent envoyer de message
					//majEtat2V2(result[0], StateMaze.valueOf( result[2]));


					//System.out.println("<----Message received from "+msg.getSender()+" ,content= "+msg.getContent());

					this.finished=true;	
				}
			}
			else{
				//	System.out.println("pas de message");
			}


		}



		private void gestionTresor(){
			if (tresoreEnVue && System.currentTimeMillis()- time > clock){
				tresoreEnVue = false;
				size = Integer.MAX_VALUE ;
				System.out.println(myAgent.getLocalName() + " va chercher le tresore");
				// enlever le behavior d'exploration et mettre le behavior de recherche de tresore
				// Stoper le explorer behavior et commence le final destiantion behavior
				if (comportement.containsKey("destination"))
					myAgent.removeBehaviour(comportement.get("destination"));
				if (comportement.containsKey("explore"))
					myAgent.removeBehaviour(comportement.get("explore"));
				comportement.put("destination", new littlestWayBehaviour(myAgent, getCurrentPosition(), destination, realEnv));
				myAgent.addBehaviour(comportement.get("destination"));
			}
		}




		private void majMap3(String pos ,String dest){

			//				if (graph.getNode(pos)==null)
			//				{
			//					graph.addNode(pos);
			//					Node destNode = graph.getNode(pos);
			//					destNode.addAttribute("ui.label", pos);
			//					destNode.addAttribute("ui.class","node"); 
			//				}
			//				if (graph.getNode(dest)==null)
			//				{
			//					graph.addNode(dest);
			//					Node destNode = graph.getNode(dest);
			//					destNode.addAttribute("ui.label", dest);
			//					destNode.addAttribute("ui.class","node"); 
			//				}

			if(!maze.containsKey(pos))
			{
				HashMap<String,Integer> temp = new HashMap<String,Integer>();
				temp.put(dest, 1);
				maze.put(pos, temp);
			}
			if(!maze.get(pos).containsKey(dest))
			{
				maze.get(pos).put(dest, 1);
				//				if(graph.getEdge(pos+dest)==null && dest!=pos)
				//				graph.addEdge(pos+dest, dest, pos);
			}

			if(!maze.containsKey(dest))
			{
				HashMap<String,Integer> temp = new HashMap<String,Integer>();
				temp.put(pos, 1);
				maze.put(dest, temp);
			}
			if(!maze.get(dest).containsKey(pos))
			{
				maze.get(dest).put(pos, 1);
				//				if(graph.getEdge(pos+dest)==null && dest!=pos)
				//				graph.addEdge(pos+dest, dest, pos);
			}
		}

		private void majEtat2 (String pos , StateMaze etat){
			for (String s : maze.get(pos).keySet()){
				if(etatNoeud.get(s) != StateMaze.Rien && etatNoeud.get(s) != StateMaze.Visiter){
					etatNoeud.put(s, etat);
				}
				for (String s2 : maze.get(s).keySet()){
					if(etatNoeud.get(s2) != StateMaze.Rien && etatNoeud.get(s2) != StateMaze.Visiter){
						etatNoeud.put(s2, etat);
					}
				}
			}
		}

		private void compareDistance (String msg ) {
			String[]result=  msg.split("@");
			if (Integer.valueOf(result[1]) < size) tresoreEnVue = false ;
			if (Integer.valueOf(result[1]) == size && Integer.valueOf(result[2]) > getBackPackFreeSpace() )tresoreEnVue = false ;

		}

		private void  calculTreasure(String position , int value){
			if (getBackPackFreeSpace()  ==0){
				return ;
			}
			time = System.currentTimeMillis();
			tresoreEnVue = true ;
			System.out.println("test");
			//size = PlusCourtChemin(getCurrentPosition(), position).size();
			System.out.println("test1");
			myAgent.addBehaviour(new TalkCapacityTreasurBehaviour(myAgent, realEnv, 10, getBackPackFreeSpace()));
			System.out.println("test2");

		}

		protected void accuserDeReception(){
			
			 MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
			 ACLMessage msg = this.myAgent.receive(msgTemplate);
			while (msg != null){
				if (boiteEnvoie.size() != 0)boiteEnvoie.remove(boiteEnvoie.size() -1);
				
				msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
				msg = this.myAgent.receive(msgTemplate);
			}
		}
		
		protected void envoieAccuser(AID sender){
			
			final ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
			msg.setSender(this.myAgent.getAID());


			msg.addReceiver(sender); // hardcoded= bad, must give it with objtab				 

			this.myAgent.sendMessage(msg);
			
		}


		private void majEtat2V2 (String pos , StateMaze etat){
			if(etatNoeud.get(pos) != StateMaze.Rien){
				etatNoeud.put(pos, etat);
			}
		}

		public boolean done() {
			//addBehaviour(new ListenerBehaviour(myAgent));
			return finished;
		}
	}

	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 4 :  THE TALKER OF TREASURE
	 * 
	 * 
	 ********************************************************/


	public class TalkTreasurBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;

		private StateMaze state;
		private int valeurT;
		private abstractAgent myAgent;

		public TalkTreasurBehaviour (final abstractAgent myagent,Environment realEnv,String pos, int valeurT) {
			super();
			this.realEnv=realEnv;
			this.state = state ;
			this.valeurT = valeurT;
			this.myAgent = myagent;

		}



		@Override
		public void action() {
			//Create a message in order to send it to the choosen agent


			//récupère les adresses de ses gens
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd  = new ServiceDescription();
			sd.setType( "AventurierDeLextreme" ); /* le même nom de service que celui qu'on a déclaré*/
			dfd.addServices(sd);

			DFAgentDescription[] result;
			try {
				result = DFService.search(this.myAgent, dfd);
				AID a = new AID() ;
				for (int i=0 ; i < result.length; i ++){

					a = result[i].getName();


					final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());


					msg.addReceiver(a); // hardcoded= bad, must give it with objtab				 


					msg.setContent("Treasure@"+ pos+"@"+valeurT );
					this.myAgent.sendMessage(msg);
				}


			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}




			this.finished=true;
		}



		@Override
		public boolean done() {
			//myAgent.addBehaviour (new ListenerBehaviour (myAgent));
			return finished;
		}



	}

	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 5 :  THE TALKER OF CAPACITY
	 * 
	 * 
	 ********************************************************/


	public class TalkCapacityTreasurBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;

		private StateMaze state;
		private int valeurT;
		int size;
		public abstractAgent myAgent;

		public TalkCapacityTreasurBehaviour (final abstractAgent myagent,Environment realEnv,int size, int valeurT) {
			super();
			this.realEnv=realEnv;
			this.state = state ;
			this.valeurT = valeurT;
			this.size = size;
			this.myAgent = myagent;
		}



		@Override
		public void action() {
			//Create a message in order to send it to the choosen agent


			//récupère les adresses de ses gens
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd  = new ServiceDescription();
			sd.setType( "AventurierDeLextreme" ); /* le même nom de service que celui qu'on a déclaré*/
			dfd.addServices(sd);

			DFAgentDescription[] result;
			try {
				result = DFService.search(this.myAgent, dfd);
				AID a = new AID() ;
				for (int i=0 ; i < result.length; i ++){

					a = result[i].getName();

					if (!a.equals(this.myAgent.getAID()) ){
						final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());


						msg.addReceiver(a); // hardcoded= bad, must give it with objtab				 

						System.out.println(myAgent.getLocalName()+" :"+"Capacity@"+ size+"@"+valeurT );
						msg.setContent("Capacity@"+ size+"@"+valeurT );
						this.myAgent.sendMessage(msg);
					}
				}

			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}




			this.finished=true;
		}



		@Override
		public boolean done() {
			//myAgent.addBehaviour (new ListenerBehaviour (myAgent));
			return finished;
		}
	}



	/**********************************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 6 : THE TREASURE FINDER
	 * 
	 * 
	 **********************************************************************/

	public class littlestWayBehaviour extends TickerBehaviour{
		/**
		 * When an agent want to go in some place , he must know the way
		 * This behavior is the method to go to a A point to a B point
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;


		private Environment realEnv;

		ArrayList<String> chemin ;
		int position ;
		abstractAgent myAgent;

		public littlestWayBehaviour(final abstractAgent myagent, String depart, String destination,Environment realEnv) {
			
			super(myagent, clock);
			//			this.maze = maze;
			//			this.etatNoeud = etatNoeud;

			this.realEnv = realEnv;
			this.myAgent = myagent;

			position =0;
			chemin = plusCourtChemin( destination);
//			System.out.println("RECHERCHE DE TRESOR !!!for " + myagent.getLocalName());
//			String s = "";
//			for (String s1 : chemin){
//				s+= "->"+s1;
//			}
//			System.out.println(s);
		}


		protected void onTick() {
			System.out.println("pcc"+pos);

			// ecoute ses messages
			myAgent.addBehaviour (new ListenerBehaviour (myAgent));

			String myPosition=getCurrentPosition();


			List<Attribute> lattribute= realEnv.observe(myPosition, this.myAgent.getLocalName()).get(0).getR();
			for(Attribute a:lattribute){
				switch (a) {
				case TREASURE:
					System.out.println("My current backpack capacity is:"+ getBackPackFreeSpace());
					System.out.println("Value of the treasure on the current position: "+a.getValue());
					System.out.println("I," +  this.myAgent.getLocalName() + " grabbed the treasure entirely :"+pick());

					break;
				default:
					break;
				}
			}			



			if (myPosition!="" ){
				if (position >= chemin.size()-1){
					comportement.put("explore", new ExplorerBehaviour(myAgent, realEnv));
					myAgent.addBehaviour(comportement.get("explore"));
					myAgent.removeBehaviour(this);
					comportement.remove("destination");
					return ;
				}
				if  ( position < chemin.size() -1){
					position += 1;
					move(myPosition, chemin.get(position));
				}

			}

		}


	}
	
	
	
	/*******************************************************
	 * 
	 * 
	 * 				BEHAVIOURS 7 :  THE TALKER OF MEMORY
	 * 
	 * 
	 ********************************************************/


	public class TalkmemoryBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;

		private StateMaze state;
		private int valeurT;
		int size;
		public abstractAgent myAgent;

		public TalkmemoryBehaviour (final abstractAgent myagent,Environment realEnv) {
			super();
			this.realEnv=realEnv;
			this.myAgent = myagent;
		}



		@Override
		public void action() {
			//Create a message in order to send it to the choosen agent


			//récupère les adresses de ses gens
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd  = new ServiceDescription();
			sd.setType( "AventurierDeLextreme" ); /* le même nom de service que celui qu'on a déclaré*/
			dfd.addServices(sd);

			DFAgentDescription[] result;
			try {
				for (ACLMessage msg : boiteEnvoie){
					System.out.println(myAgent.getLocalName() +" :" + msg.getContent());
						this.myAgent.sendMessage(msg);
					}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}




			this.finished=true;
		}



		@Override
		public boolean done() {
			//myAgent.addBehaviour (new ListenerBehaviour (myAgent));
			return finished;
		}
	}


	


}
