package ch.heigvd.wem.linkanalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import ch.heigvd.wem.labo1.Labo1;
import ch.heigvd.wem.linkanalysis.*;

public class lab2 {
	private static final int List = 0;
	private static final int String = 0;
	//private static String filename =null;
	private static AdjacencyMatrix am;
	private static HashMap<String,Integer> map;
	private static String filename = Labo1.class.getClassLoader().getResource("graph_example.txt").getPath().replaceAll("%20", " ");
	//private static String filename = Labo1.class.getClassLoader().getResource("graph_reference.txt").getPath().replaceAll("%20", " ");
	
		
	public static void main(String[] args) throws Exception{
		System.out.println("filename"+filename);
		GraphFileReader reader = new GraphFileReader(filename);
		am=reader.getAdjacencyMatrix();
		map=reader.getNodeMapping();
		System.out.println(am);
		
		System.out.println(map);
		double[][] InOutDegree=new double[am.size()][2];
		double auth = 0, hubs=0, normauth = 0,normhubs = 0;
		Vector<Double> ac=new Vector<Double>(0);
		Vector<Double> hc=new Vector<Double>(0);
		
		for(int i=0;i<am.size();i++){
			ac.add(0.0);
			hc.add(0.0);
		}
		
		LinkAnalysis lin=new LinkAnalysis();
		Vector<Double> vhubs = new Vector<Double>(am.size());
		vhubs=lin.calculateHc(am,ac);
		Vector<Double> vauths = new Vector<Double>(am.size());
		vauths=lin.calculateAc(am,hc);
		
		System.out.println("vauths");
		System.out.println(vauths);
		System.out.println("vhubs");
		System.out.println(vhubs);
		
		HashMap<Integer,Double> AuthHash=new HashMap<Integer,Double>();
        HashMap<Integer,Double> HubsHash=new HashMap<Integer,Double>();
        
        System.out.println( "Node  :   hubs  	auths  ");
        for(int i=0 ; i<am.size() ; i++){
    		System.out.printf( "%d : \t %.8f \t %.8f \n", i+1, vhubs.get(i), vauths.get(i) );
    		AuthHash.put(i+1, vhubs.get(i));
    		HubsHash.put(i+1, vauths.get(i));
        }
		
	}

}