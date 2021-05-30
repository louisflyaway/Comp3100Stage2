//44434065 Runde Jia

import java.net.*;
import java.io.*;
import java.lang.reflect.Array;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Client {
	private Socket socket;
    private BufferedReader inputStream;
    private DataOutputStream outputStream;
    private ArrayList<Server> serverList = new ArrayList<Server>();
    private Server[] servers = new Server[1];
    private int large = 0;
    private String message;
    private Boolean completed = false;
    
    public static void main(String args[]) {
    	Client client = new Client ("127.0.0.1", 50000);
    	
    	client.Run();    
    }
    
    public Client (String address, int port) {
    	//Open a socket
        try {
            socket = new Socket(address, port);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch(UnknownHostException z) {
            System.out.println("Error " + z);
        }
        catch(IOException x) {
            System.out.println("Error " + x);
        }
    }
    
    //To parse system.xml
    public void XMLParse() {
        try {
			File systemXML = new File("ds-system.xml");

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
			Document document = builder.parse(systemXML);

            document.getDocumentElement().normalize();
            
            //Store Servers information in NodeList
			NodeList serverList = document.getElementsByTagName("server");
            servers = new Server[serverList.getLength()];
            

			for (int i = 0; i < serverList.getLength(); i++) {

				Element server = (Element) serverList.item(i);
				String type = server.getAttribute("type");
				int lim = Integer.parseInt(server.getAttribute("limit"));
				int boot = Integer.parseInt(server.getAttribute("bootupTime"));
				float rate = Float.parseFloat(server.getAttribute("hourlyRate"));
				int core = Integer.parseInt(server.getAttribute("coreCount"));
				int mem = Integer.parseInt(server.getAttribute("memory"));
				int disk = Integer.parseInt(server.getAttribute("disk"));
				Server temp = new Server(i, type, lim, boot, rate, core, mem, disk);
				servers[i] = temp;
			}
			//Find first largest server
			large= FindLargest();
		} catch (Exception i) {
            System.out.println("Error " + i);
            i.printStackTrace();
		}

    }
	
	private Server getEqualizationServer(Server[] servers, ArrayList<Server> availableServer, Job job) {
        Server CheapActiveServer = null;
        Server CheapServer = null;
        //float memory = 0;
        //float maxMemory = Integer.MAX_VALUE;
        double maxFitRate =  Integer.MAX_VALUE;
        double fitRate = 0;
        float responRate = 0;
 //./ds-server -c ds-config-s2-1.xml -v all
 //./ds-server -c config_simple1.xml -v all
 //./ds-server -c ds-config-s3-6.xml -v all

        //increasing sort server by id
        orderServersById(availableServer);
        //all of the server type in system.xml
        for (int i = 0; i < servers.length; i++) {
            String curType = ((Server) Array.get(servers,i)).getType();
            
            for (int j = 0; j < availableServer.size(); j++) {
                Server temp = availableServer.get(j);
                //only read server type i
                //16.5 ff
                if (temp.getType().equals(curType))
                {
                    
                    //(temp.getAvailableTime() < minAvail)
                    //minAvail = temp.getAvailableTime();
                    if (serverHasSufficientResources(temp, job))
                    {
                        double coreRate, memoryRate, diskRate;
                    coreRate = (temp.getCoreCount() - job.cores())/job.cores();
                    memoryRate = (temp.getMemory() - job.getMemory())/job.getMemory();
                    diskRate = (temp.getDisk() - job.getDisk())/job.getDisk();
                    responRate = (temp.getAvailableTime() + job.getEstRunTime())/job.getEstRunTime();
                    fitRate = coreRate + memoryRate + diskRate + responRate;

                   // System.out.print(fitRate+" ");
                        if(fitRate <= maxFitRate){
                            maxFitRate = fitRate;
                            CheapServer = temp;
                            //break;
                           // System.out.print(fitRate+" ");
                            }
                    }
                    
                }
            }
        }
        
        if(CheapServer != null) return CheapServer;
        else {
            // for each server type in the order of appearance in system.xml
            for (int i = 0; i < servers.length; i++) {
                String curType = ((Server) Array.get(servers,i)).getType();

                // in the order of id
                for (int j = 0; j < servers.length; j++) {
                    Server temp = availableServer.get(j);
                    if (temp.getType().equals(curType))
                    {
                        
                        if (temp.isActive() && serverHasSufficientResources(((Server) Array.get(servers,i)), job) )
                        {
                            double coreRate, memoryRate, diskRate;
                    coreRate = (temp.getCoreCount() - job.cores())/job.cores();
                    memoryRate = (temp.getMemory() - job.getMemory())/job.getMemory();
                    diskRate = (temp.getDisk() - job.getDisk())/job.getDisk();
                    responRate = (temp.getAvailableTime() + job.getEstRunTime())/job.getEstRunTime();
                    fitRate = coreRate + memoryRate + diskRate + responRate;
                            if(fitRate <= maxFitRate){
                                
                            maxFitRate = fitRate;
                            CheapActiveServer = temp;
                            //break;
                            }
                        }
                    }
                }
            }
            
            return CheapActiveServer;
        }
        }
    
    //Follow the ds-sim protocol, including handshake and loop
    public void Run() {
		Server sendToEFServer;
    	//Handshake
		send("HELO"+"\n");
		message = receive();
		send("AUTH " + System.getProperty("user.name")+"\n");
		message = receive();
		XMLParse();
		send("REDY"+"\n");
		message = receive();

		//If there is no job, quit server
		if (message.equals("NONE")) {
			quit();
        } 
        else {
        	//If jobs are not completed, keeps receive message from server
			while (!completed) {
				if (message.equals("OK")) {
					send("REDY"+"\n");
					message = receive();
                }
                
				//If job is completed, received next job
				while(message.startsWith("JCPL")) {
					send("REDY"+"\n");
					message = receive();
				}
				//No job need to complete, go to the serverQuit 
				if (message.equals("NONE")) {
					completed = true;
					break;
                }
                
                //Split message to job information
				String[] jobInfo = message.split(" "); 
				Job job = new Job(Integer.valueOf(jobInfo[1]), Integer.valueOf(jobInfo[2]),
						Integer.valueOf(jobInfo[3]), Integer.valueOf(jobInfo[4]), Integer.valueOf(jobInfo[5]),
						Integer.valueOf(jobInfo[6]));
				
				//When received job information, respond to the server
				send("RESC All"+"\n"); 
                message = receive();
				send("OK"+"\n");

                message = receive();
				serverList = new ArrayList<Server>();
				
				//If not receiving '.', keep adding servers to the list
				while (!message.equals(".")) {
					
					String[] serverInfo = message.split("\\s+");
					
                    serverList.add(
							new Server(serverInfo[0], Integer.parseInt(serverInfo[1]), Integer.parseInt(serverInfo[2]),
									Integer.parseInt(serverInfo[3]), Integer.parseInt(serverInfo[4]),
									Integer.parseInt(serverInfo[5]), Integer.parseInt(serverInfo[6])));
					send("OK"+"\n");
                    message = receive();
				}
				sendToEFServer = getEqualizationServer(servers,serverList,job);
					//Schedule jobs to the first largest server
					send("SCHD " + job.getId() + " " + sendToEFServer.getType() + " " + sendToEFServer.getId() +"\n");
					message = receive();					
			}
        }
		quit();
	}
	
    //Send message to the server
    public void send(String msg) {
        
        try {
            outputStream.write(msg.getBytes());
            outputStream.flush();


        } catch (IOException i) {
            System.out.println("Error " + i);
        }

    }

    public String receive(){ 
		String msg = "";
		try {
			while (!inputStream.ready()) {
			}
			while (inputStream.ready()) {
				msg += inputStream.readLine();
            }
            
			message = msg;
		} catch (IOException i) {
            System.out.println("Error " + i);
        }
		return msg;
    }

    //Quit server, close streams and socket
    public void quit(){
        try {
            send("QUIT"+"\n");
            message = receive();
            if (message == "QUIT") {
                inputStream.close();
                outputStream.close();
                socket.close();
            }
        } catch (IOException i) {
            System.out.println("Error " + i);
        }
    }
    
    //Algorithm for finding the first largest server
    public int FindLargest() {
        int large = servers[0].id;

		for (int i = 0; i < servers.length; i++) {
			if (servers[i].coreCount > servers[large].coreCount) {
				large = servers[i].id;
			}
		}
		return large;
    }
	private static void orderServersById(List<Server> servers) {
        Collections.sort(servers, new Comparator() {
            public int compare(Object o1, Object o2) {
                Integer id1 = ((Server) o1).getId();
                Integer id2 = ((Server) o2).getId();
                return id1.compareTo(id2);
            }
        });
    }

    private static boolean serverHasSufficientResources(Server server, Job job) {
        return ((server.getCoreCount() >= job.cores()) &&
                (server.getDisk() >= job.getDisk()) &&
                (server.getMemory() >= job.getMemory()));
    }

}
