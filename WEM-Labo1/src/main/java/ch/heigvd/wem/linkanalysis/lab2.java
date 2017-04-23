package ch.heigvd.wem.linkanalysis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ch.heigvd.wem.labo1.Labo1;

public class lab2 {
	//private static String filename =null;
	private static AdjacencyMatrix am;
	private static HashMap<String,Integer> map;
	private static String filename = Labo1.class.getClassLoader().getResource("graph_example.txt").getPath().replaceAll("%20", " ");
	
	public static void main(String[] args) throws Exception{
		System.out.println("filename"+filename);
		GraphFileReader reader = new GraphFileReader(filename);
		am=reader.getAdjacencyMatrix();
		map=reader.getNodeMapping();
		System.out.println(am);
		//System.out.println(map);
		double[ ][ ] InOutDegree=new double[am.size()][2];;
		double auth = 0, hubs=0, normauth = 0,normhubs = 0;
		
		for(int i=0;i<am.size();i++){
			auth = 0;
			hubs=0;
    		for(int j=0;j<am.size();j++){
    			if(am.get(i, j) == 1){
    				InOutDegree[i][1]++; // calculate Authority 
    				auth += InOutDegree[i][1];
    				InOutDegree[j][0]++; // calculate Hubs
    				hubs += InOutDegree[j][0];
               	}
    		}
    		normauth += Math.pow(auth, 2);
    		normhubs += Math.pow(hubs, 2);
    	}
		
		normauth = Math.sqrt(normauth);
		normhubs = Math.sqrt(normhubs);
        for(int i=0 ; i<am.size() ; i++){
        	InOutDegree[i][1] = InOutDegree[i][1]/normauth;
        	InOutDegree[i][1] = InOutDegree[i][1]/normhubs;
        }
        
        for(int i=0 ; i<am.size() ; i++){
        	if(InOutDegree[i][0] != 0 || InOutDegree[i][1] != 0){
        		System.out.printf( "Node %d : \n\t %.8f \t %.8f \n", i+1, InOutDegree[i][0], InOutDegree[i][1] );
        		//AuthHash.put(i+1, AuthHubs[i][0]);
        		//HubsHash.put(i+1, AuthHubs[i][1]);
        	}
        }
        
	}
	
}
