package riddles;

public class C extends B {
	
	private int i;
	private int j;

	public C(int i, int j) {
		super(i,j);
		
	}

	@Override
	public int compareTo(A other) {
		int compareJ = ((Integer)this.j).compareTo((Integer)other.j);
		if (compareJ != 0)
			return compareJ;
		else
			return ((Integer)this.i).compareTo((Integer)other.i);
	}



}