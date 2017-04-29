package ch.heigvd.wem.linkanalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class provides static methods to make link analysis.
 */
public class LinkAnalysis {
	
	

	/**
	 * Calculates and returns the hub vector.
	 * @param m Adjacency matrix.
	 * @param ac Auhtority vector of the previous step.
	 * @return Hub vector.
	 */
	public static Vector<Double> calculateHc (AdjacencyMatrix m, Vector<Double> ac) {

		Vector<Double> result = new Vector<Double>(m.size());
		double hubs=0;
		double norm = 0.0;
		int k=5;
		if (k>m.size()){
			k=m.size();
		}
		/* A IMPLEMENTER */
		for(int i=0;i<m.size();i++){
			result.add(ac.get(i));
		}
		
		for (int i=0; i<k; i++) {
			norm = 0.0;
			hubs = 0;
    		for(int j=0;j<m.size();j++){
    			if(m.get(i, j) == 1){
    				result.set(j, result.get(j)+1); // calculate Hubs
    				hubs += result.get(j);
               	}
    		}
    		norm += Math.pow(hubs, 2);
		}
		norm = Math.sqrt(norm);
        for(int i=0 ; i<m.size() ; i++){
        	//result.set(i,result.get(i)/norm);
        }
		return result; 
	}

	/**
	 * Calculates and returns the authority vector.
	 * @param m Adjacency matrix.
	 * @param hc Hub of the previous step.
	 * @return Authority vector.
	 */
	public static Vector<Double> calculateAc (AdjacencyMatrix m, Vector<Double> hc) {

		Vector<Double> result = new Vector<Double>(m.size());
		double auth=0;
		double norm = 0.0;
		int k=5;
		if (k>m.size()){
			k=m.size();
		}
		/* A IMPLEMENTER */
		for(int i=0;i<m.size();i++){
			result.add(hc.get(i));
		}
		
		for (int i=0; i<k; i++) {
			norm = 0.0;
			auth = 0;
    		for(int j=0;j<m.size();j++){
    			if(m.get(i, j) == 1){
    				result.set(i, result.get(i)+1); // calculate Hubs
    				auth += result.get(i);
               	}
    		}
    		norm += Math.pow(auth, 2);
		}
		norm = Math.sqrt(norm);
        for(int i=0 ; i<m.size() ; i++){
        	//result.set(i,result.get(i)/norm);
        }
		return result; 
	}

	/**
	 * Calculates and returns the pagerank vector.
	 * @param m Adjacency matrix.
	 * @param pr Pagerank vector of the previous step.
	 * @return Pagerank vector.
	 */
	public static Vector<Double> calculatePRc (AdjacencyMatrix m, Vector<Double> pr) {

		Vector<Double> result = new Vector<Double>(m.size());

		Vector<Double> _E = new Vector<Double>(m.size());
		for (int i=0 ; i<m.size() ; i++) _E.add(0.15D / m.size());
		// set to 0 if can be reached
		for (int i=0 ; i<m.size() ; i++)
			for (int j=0 ; j<m.size() ; j++)
				if (m.get(i, j) != 0D) _E.set(j, 0D);
		//now _E contains 0.15/|V| for unlinked pages and 0 elsewhere
		
		//Prc = normalize(0.85 . (Mt . pr) + E)
		result = normalize(add(multiply(m.transpose().multiply(pr), 0.85D), _E));

		return result; 
	}
	
	private static Vector<Double> multiply(Vector<Double> vector, Double constant) {
		Vector<Double> output = new Vector<Double>(vector.size());
		for (int i=0 ; i<vector.size() ; i++) output.add(constant * vector.get(i));
		return output;
	}
	
	private static Vector<Double> add(Vector<Double> vector, Double constant) {
		Vector<Double> output = new Vector<Double>(vector.size());
		for (int i=0 ; i<vector.size() ; i++) output.add(constant + vector.get(i));
		return output;
	}
	
	private static Vector<Double> add(Vector<Double> vector, Vector<Double> other) {
		Vector<Double> output = new Vector<Double>(vector.size());
		for (int i=0 ; i<vector.size() ; i++) output.add(other.get(i) + vector.get(i));
		return output;
	}
	
	private static double sum(Vector<Double> vector) {
		double sum = 0D;
		for (Double value : vector) sum+= value;
		return sum;
	}
	
	private static Vector<Double> normalize(Vector<Double> vector) {
		Vector<Double> output = new Vector<Double>(vector.size());
		double sum = sum(vector);
		for (int i=0 ; i<vector.size() ; i++) output.add(vector.get(i)/sum);
		return output;
	}
}
