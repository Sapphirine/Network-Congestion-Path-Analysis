package org.network.Servelet;
import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
//import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.WeightedPath;

//import org.neo4j.graphdb.traversal.TraversalDescription;
//import org.neo4j.graphdb.traversal.TraversalDescription;

import org.apiacoa.graph.clustering.DoCluster;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Scanner;



//Code Ref From neo4j tutorial at:
//http://neo4j.com/docs/stable/tutorials-java-embedded-hello-world.html

public class NetworkDataAnalysis {
	
	private static final String DB_PATH = "target/NETDB_UNIQUE_Length";
	// START SNIPPET: vars
	GraphDatabaseService graphDb;
	Node txNode;
	Node rxNode;
	Relationship relationship;
		
	static boolean node_exists = false;

	
	
	// END SNIPPET: vars	
	private static enum RelTypes implements RelationshipType
	{
	    CONNECTS
	}
	
	

	public static void main(final String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Loader and Tester for Neo4J Network Traffic Analysis Tool");
		NetworkDataAnalysis netTool = new NetworkDataAnalysis();
		netTool.createDb("try.csv");
		//netTool.removeData();
		for(int i = 0;i<10;i++){
			node_exists = false;
			Scanner reader = new Scanner(System.in);
			System.out.println("Enter the start node:");
			//get user input for a
			int start_input=reader.nextInt();
			System.out.println("Enter the end node:");
			int end_input=reader.nextInt();
		    netTool.getPath(start_input,end_input);
		    netTool.shutDown();
		}
		netTool.shutDown();
	}
	
	
	
	
	@SuppressWarnings({ "deprecation", "unused", "unchecked" })
	void createDb(String csvFile)
	 {
	    	
	    	System.out.println("Loading Data.... (This may take a few minutes)");
		    // START SNIPPET: startDb
	    	//Delete the current DB File before loading data
		    deleteFileOrDirectory( new File( DB_PATH ) );
		    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		    //Register Shutdown incase of error, exception, or ctrl-c db is preserved
		    registerShutdownHook( graphDb );
		    
		    //Setup Index File For Looking Up Nodes CSV ID and translating to NEO4J ID		
		    IndexManager index = graphDb.index();		    
            int node_count = 0;		    
		    
		    //Load CSV File
	    	//String csvFile = "/home/dcadiga/e6893/project/cycle-aslinks.l7.t1.c003659.20141123_PARSED.csv";
	    	//write to txt file for clustering
            BufferedReader br = null;
	    	PrintWriter writer = null;
	    	String line = "";
	    	String cvsSplitBy = ",";
	    	
			JSONObject obj = null;
			JSONArray adj = null;
	    	FileWriter file = null;
	    	
	    	try {     
	    		br = new BufferedReader(new FileReader(csvFile));
	    		writer = new PrintWriter("edge.txt");
	    		
    	    	String path = csvFile;
    	    	String pathArr[] = path.split("/Final_Project_6893/");
		        file = new FileWriter(pathArr[0] + "/Final_Project_6893/WebContent/json.txt");
		        file.write("[");
		        
	    		//Read file line by line
	    		while ((line = br.readLine()) != null) {
	     
	    			// use comma as separator
	    			String[] nodes = line.split(cvsSplitBy);
	    			writer.println(nodes[0] + " " + nodes[1] + " " + nodes[2]);
	    					
	    			//Begin Graph DB Transaction - runs and commits on every line   
	    			Transaction tx = graphDb.beginTx(); 
	    			try {
	    				Index<Node> id_index = index.forNodes( "id" );
	    				txNode=getOrCreateUserWithUniqueFactory(nodes[0],graphDb);
	    				//txNode=graphDb.createNode();
	    				txNode.setProperty("id",nodes[0]);
	    				id_index.add(txNode,"id",txNode.getProperty("id"));
	    				//rxNode=graphDb.createNode();
	    				rxNode=getOrCreateUserWithUniqueFactory(nodes[1],graphDb);
	    				rxNode.setProperty("id",nodes[1]);
	    				id_index.add(rxNode,"id",rxNode.getProperty("id"));
	    				relationship = txNode.createRelationshipTo( rxNode, RelTypes.CONNECTS);
	    				relationship.setProperty( "length", Integer.parseInt(nodes[2]));
	    				//System.out.println("Node0 " + nodes[0] + "ID " + txNode.getId() + " , Node1 " + nodes[1] + "ID " + rxNode.getId()+  " Distance " + nodes[2]);
	    				tx.success();
	    				if(node_count % 1000 == 0){
	    			    	System.out.println("Committed Node " + node_count);  
	    				}
	    				node_count++;
	    				
	    				// store in json for vitualization
	    				if (obj == null || !obj.get("id").equals(nodes[0])) {
	    					if (obj != null) {
	    						obj.put("adjacencies", adj);
	    	    		        try {
	    	    		            file.write(obj.toJSONString() + ",");
	    	    		 
	    	    		        } catch (IOException e) {
	    	    		            e.printStackTrace();
	    	    		        }
	    					}
	    					obj = new JSONObject();
	    					obj.put("id", nodes[0]);
	    					obj.put("name", nodes[0]);
	    					JSONObject style = new JSONObject();
	    					style.put("$color", "#1B9E77");
	    					style.put("$type", "circle");
	    					obj.put("data", style);
	    					adj = new JSONArray();
	    				}
	    				
	    				JSONObject tmp = new JSONObject();
	    				tmp.put("nodeTo", nodes[1]);
	    				tmp.put("nodeFrom", nodes[0]);
	    				tmp.put("data", Integer.parseInt(nodes[2]));
	    				adj.add(tmp);
	    					    			      
	    			}
	    			finally {
	    				tx.close();
	    			}
	    			
	    		 	
	    		}//End While Loop For File
				obj.put("adjacencies", adj);
		        try {
		            file.write(obj.toJSONString() + "]");
		 
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
	     
	    	} catch (FileNotFoundException e) {
	    		e.printStackTrace();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	} finally {
	    		if (br != null) {
	    			try {
	    				br.close();
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
	    		}
				writer.close();
				try {
					file.flush();
					file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}         
	    	}
	     
	    	System.out.println("Done");
      

	 }
	
	
	
	 void removeData()
	 {
		 try ( Transaction tx = graphDb.beginTx() )
	 {
			 // START SNIPPET: removingData
			 // let's remove the data
			 //firstNode.getSingleRelationship( RelTypes.KNOWS, Direction.OUTGOING ).delete();
			 //firstNode.delete();
			 //secondNode.delete();
			 // END SNIPPET: removingData
			 tx.success();
	 	}
	 }
	 
	 
	 
	 void shutDown()
	 {
		 System.out.println();
		 System.out.println( "Shutting down database ..." );
		 // START SNIPPET: shutdownServer
		 if (graphDb != null) {
			 graphDb.shutdown();
		 }
		 // END SNIPPET: shutdownServer
	 }
	 
	 
	 
	 // START SNIPPET: shutdownHook
	 private static void registerShutdownHook( final GraphDatabaseService graphDb )
	 {
		 // Registers a shutdown hook for the Neo4j instance so that it
		 // shuts down nicely when the VM exits (even if you "Ctrl-C" the
		 // running application).
		 Runtime.getRuntime().addShutdownHook( new Thread()
		 {
			 @Override
			 public void run()
			 {
				 graphDb.shutdown();
			 }
		 } );
	 }
	 
	 
	 
	 // END SNIPPET: shutdownHook
	 private static void deleteFileOrDirectory( File file )
	 {
		 if ( file.exists() )
		 {
			 if ( file.isDirectory() )
			 {
				 for ( File child : file.listFiles() )
				 {
					 deleteFileOrDirectory( child );
				 }
			 }
			 file.delete();
		 }
	 }
	 
	 
	 
	 public Node getOrCreateUserWithUniqueFactory( String id, GraphDatabaseService graphDb )
	 {
		 //Create a uniqueFactory (graphDB) where the node is created if new or the node id is returned if this node
		 //exists already
	     UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory( graphDb, "SystemNodes" )
	     {

			@Override
			protected void initialize(Node arg0, Map<String, Object> arg1) {
				// TODO Auto-generated method stub
				//Should do something here
				
			}
	     };
        return factory.getOrCreate( "id", id );  //create or return node id
    }
	 
	 
	 
	String getPath (int start_loc, int end_loc){
		String output = "";
		
		//Path traversal tool that uses dijkstra's algorithm to find the shortest path by relationship property length
	    if (graphDb == null) {
	    	graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
	    }
	    registerShutdownHook( graphDb );
	    
	    //Setup Index File For Looking Up Nodes CSV ID and translating to NEO4J ID	
	    IndexManager index = graphDb.index();

		PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection( RelTypes.CONNECTS, Direction.OUTGOING ), "length" );
		Transaction tx = graphDb.beginTx(); 
		try {
			Index<Node> id_index = index.forNodes( "id" );
			IndexHits<Node> hits = id_index.get( "id", Integer.toString(start_loc) );
			Node start = null;
			Node end = null;
			start = hits.getSingle();
			if(start != null){
				node_exists = true;
			}
			else{
				node_exists = false;
				output = "Start Node Does Not Exist";
				System.out.println("Start Node Does Not Exist");
			}
			     
			hits = id_index.get( "id", Integer.toString(end_loc) );
			end = hits.getSingle();
			if((end != null) && (node_exists)){
			        
			}
			else{
				node_exists = false;
				output = "End Node Does Not Exist";
				System.out.println("End Node Does Not Exist");
			}
			    
			if((node_exists) && (start != null) && (end != null)){
				WeightedPath path = finder.findSinglePath(start, end );
				if(path == null){
					output = "No Path from " + start_loc + " to " + end_loc;
					System.out.println("No Path from " + start_loc + " to " + end_loc );
				}
				else{
					output = "Path found:\n";
					System.out.println(path);
					for ( Node node : path.nodes() ) {
			   	     	//Print out all of the nodes in the path
			   	     	output += node.getProperty( "id" ) + ",  " ; 
					    System.out.println( node.getProperty( "id" ) );
					}
				}
				tx.success();
			}
			else {
				//System.out.println("Something is wrong");
			}
			    	
		}
		finally {
			tx.close();
		}

		return output;  

	}
		
	void clustering() {
		System.out.println("clustering...");
		DoCluster cl = new DoCluster();
		String[] args = new String[]{"-graph", "edge.txt", "-mod", "mod.txt", "-part", "cluster.txt", "-random", "100", "-recursive"};
		try {
			cl.main(args);
		} catch (IOException e) {
    		e.printStackTrace();
    	} 
		System.out.println("clustering done");
	}
}
