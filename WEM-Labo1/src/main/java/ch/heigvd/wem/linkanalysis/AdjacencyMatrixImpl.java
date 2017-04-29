package ch.heigvd.wem.linkanalysis;

public class AdjacencyMatrixImpl extends AdjacencyMatrix {

	private double[][] elements;
	
	public AdjacencyMatrixImpl(int size) {
		elements = new double[size][size];
	}
	
	@Override
	public int size() {
		return elements.length;
	}

	@Override
	public double get(int i, int j) {
		return elements[i][j];
	}

	@Override
	public void set(int i, int j, double edge) {
		elements[i][j] = edge;
	}

	@Override
	public int addLast() {
		throw new UnsupportedOperationException();
	}

	@Override
	public AdjacencyMatrix getTransitionMatrix() {
		AdjacencyMatrix tm = new AdjacencyMatrixImpl(elements.length);
		int i=0;
		for (double[] row : elements) {
			int j=0;
			double rowTotal = 0;
			for (double edge : row) rowTotal += edge;
			for (double edge : row) {
				if (rowTotal != 0) tm.set(i, j, edge / rowTotal);
				j++;
			}
			i++;
		}
		return tm;
	}
	
	public AdjacencyMatrix transpose() {
		AdjacencyMatrixImpl trans = new AdjacencyMatrixImpl(this.size());
		for (int i=0 ; i<size() ; i++) for (int j=0 ; j<size() ; j++) trans.elements[j][i] = elements[j][i];
		return trans;
	}
	
	public AdjacencyMatrix dot(AdjacencyMatrix other) {
		if (size() != other.size()) throw new IllegalArgumentException("matrix sizes do not match");
		AdjacencyMatrixImpl product = new AdjacencyMatrixImpl(this.size());
		for (int i=0 ; i<size() ; i++) for (int j=0 ; j<size() ; j++) {
			product.elements[i][j] = elements[i][j] * other.get(i, j);
		}
		return product;
	}
	
	public AdjacencyMatrix multiply(AdjacencyMatrix other) {
		if (size() != other.size()) throw new IllegalArgumentException("matrix sizes do not match");
		AdjacencyMatrixImpl product = new AdjacencyMatrixImpl(this.size());
		for (int i=0 ; i<size() ; i++) for (int j=0 ; j<size() ; j++) {
			for (int k = 0 ; k<size() ; k++)
				product.elements[i][j] += elements[i][k] * other.get(k, j);
		}
		return product;
	}
}
