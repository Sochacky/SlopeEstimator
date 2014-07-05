package skarpa;

import java.util.ArrayList;

import static skarpa.Model.*;

public class Cycle {
	ArrayList<Integer> vxList = new ArrayList<>(); //lista vx - liczy się kolejność
	ArrayList<Integer> lnList = new ArrayList<>(); //lista linii - liczy się kolejność
	
	void addVx(int index, int v){
		vxList.add(index, v);
	}
	
	void addVx(int v){
		vxList.add(v);
	}
	
	/*void delVx(int v){
		vxList.remove(v);
	}*/
	
	static void deleteC(int c){
		cycles[c] = null;
		if(c<markerCT) markerCT = c;
	}
}
