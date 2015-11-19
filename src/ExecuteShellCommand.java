import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExecuteShellCommand {

    public static void command() throws IOException, InterruptedException {
    	
    	//String[] args = new String[] {"/bin/bash", "-c", "./Scrivania/springExample/neo4j-community-2.3.1/bin/neo4j", "console"};
    	String[] args = new String[] {"/bin/bash", "-c", "ping 8.8.8.8", "console"};
    	Process proc = new ProcessBuilder(args).start();

    }
}