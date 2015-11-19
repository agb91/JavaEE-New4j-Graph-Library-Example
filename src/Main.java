import java.io.BufferedReader;
import java.io.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.GraphDatabaseAPI;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;

public class Main {	
	
	private static GraphDatabaseService graphDb;
	private static String DB_PATH = "/home/andrea/Scrivania/springExample/dbtest";
	private static Vector<Node> nodi = new Vector<Node>();
	private static String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
	

	public enum RelTypes implements RelationshipType{
		STD, TYPE2;
	}	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		clean(DB_PATH);
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		
		setLabelSystem();
				
		Node a = addNode("primo");
		Node b = addNode("secondo");
		Node c = addNode("terzo");
		Node d = addNode("quarto");
		Node e = addNode("quinto");
		Node f = addNode("sesto");
		
		Relationship r1 = arco(a,b);
		Relationship r2 = arco(b,c);
		Relationship r3 = arco(a,d);
		Relationship r4 = arco(d,e);
		Relationship r5 = arco(a,a);
		Relationship r6 = arco(e,f);
		Relationship r7 = arco(a,f);
		Relationship r8 = arco(d,d);
		
		Iterator<Node> path = findPath(a,c);	
		
		
		
		sendNodeToServer();
		setPropertyNodeServer("4","name","quarto");
		sendNodeToServer();
		setPropertyNodeServer("5","name","quinto");
		
		setRelationshipServer("1","4","STD");
		setRelationshipServer("4","5","STD");
					
	}
	
	private static void setRelationshipServer(String node1, String node2, String type)
	{
		URI fromUri = null;
		try {
			fromUri = new URI( "http://localhost:7474/db/data/node/" + node1 + "/relationships/" );
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String endUri = "http://localhost:7474/db/data/node/"+node2;
		String relationshipJson = generateJsonRelationship( endUri ,type, "rel");
	    WebResource resource = Client.create()
	            .resource( fromUri );
	    // POST JSON to the relationships URI
	    ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
	            .type( MediaType.APPLICATION_JSON )
	            .entity( relationshipJson )
	            .post( ClientResponse.class );
	    final URI location = response.getLocation();
	    System.out.println( String.format(
	            "POST to [%s], status code [%d]",
	            fromUri, response.getStatus()) );
	    response.close();

	}
	
	private static String generateJsonRelationship(String secondNodeLocation,  
			  String relationship, String relationAttributes) 
	{  
		StringBuilder sb = new StringBuilder();  
		sb.append("{ \"to\" : \"");  
		sb.append(secondNodeLocation);  
		sb.append("\", ");  
		sb.append("\"type\" : \"");  
		sb.append(relationship);  
        sb.append("\"");  
        sb.append("}");
		return sb.toString();  
	}  
	
	private static void setPropertyNodeServer(String id , String propertyName, String propertyValue)
	{
		String propertyUri = "http://localhost:7474/db/data/node/" + id + "/properties/" + propertyName;
		WebResource resource = Client.create()
		        .resource( propertyUri );
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .entity( "\"" + propertyValue + "\"" )
		        .put( ClientResponse.class );

		System.out.println( String.format( "PUT to [%s], status code [%d]",
		        propertyUri, response.getStatus() ) );
		response.close();
	}
	
	private static URI sendNodeToServer()
	{
		final String nodeEntryPointUri = SERVER_ROOT_URI + "node";
		// http://localhost:7474/db/data/node

		WebResource resource = Client.create()
		        .resource( nodeEntryPointUri );
		// POST {} to the node entry point URI
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .entity( "{}" )
		        .post( ClientResponse.class );

		URI location = response.getLocation();
		System.out.println( String.format("POST to [%s], status code [%d]", nodeEntryPointUri, response.getStatus() ) );
		response.close();

		return location;
	}
	
	private static Iterator<Node> findPath(Node s, Node e)
	{
		Iterator<Node> it;
		try ( Transaction tx = graphDb.beginTx() )
		{
			PathFinder<Path> finder =
					GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection(
							RelTypes.STD , Direction.OUTGOING ), 15 );
			Iterable<Path> paths = finder.findAllPaths( s, e );
			Path path = paths.iterator().next();
			it = path.nodes().iterator();
			if(it.hasNext())
			{
				System.out.println(it.next().getProperty("name"));
			}
			if(it.hasNext())
			{
				System.out.println(it.next().getProperty("name"));
			}
			if(it.hasNext())
			{
				System.out.println(it.next().getProperty("name"));
			}
			tx.success();
		}	
		return it;
	}
	
	private static Relationship arco(Node n1, Node n2)
	{
		Relationship relationship;
		try ( Transaction tx = graphDb.beginTx() )
		{
			relationship = n1.createRelationshipTo( n2, RelTypes.STD );
			relationship.setProperty( "type", "normal " );
			tx.success();
		}	
		System.out.println("arc created");
		return relationship;
	}
	
	private static void clean(String path)
	{
		try {
			FileUtils.deleteRecursively(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Node findNodeByName(String nameToFind)
	{
		ArrayList<Node> userNodes = new ArrayList<>();
		Label label = DynamicLabel.label( "Nome" );
		try ( Transaction tx = graphDb.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		            graphDb.findNodes( label, "name", nameToFind ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }

		        for ( Node node : userNodes )
		        {
		            System.out.println( "trovato nodo: " + node.getProperty( "name" ) );
		        }
		    }
		}
		return userNodes.get(0);

	}
	
	private static void setLabelSystem()
	{
		IndexDefinition indexDefinition;
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Schema schema = graphDb.schema();
		    indexDefinition = schema.indexFor( DynamicLabel.label( "Nome" ) )
		            .on( "name" )
		            .create();
		    tx.success();
		}
		
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Schema schema = graphDb.schema();
		    schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
		}		
	}
	
	private static Node addNode(String name)
	{
		Node userNode;
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Label label = DynamicLabel.label( "Nome" );
	        userNode = graphDb.createNode( label );
	        userNode.setProperty( "name", name);
		    System.out.println( "node created" );
		    tx.success();
		}
		return userNode;
	}
	
	
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
}
