

import java.util.Comparator;

public class TreePontComparator implements Comparator<Jogador>{

	@Override
	public int compare(Jogador j1, Jogador j2) {
		if(j1.getPont() > j2.getPont()) return 1;
		if(j1.getPont() < j2.getPont()) return -1;
		else return 0;
 	}

}
