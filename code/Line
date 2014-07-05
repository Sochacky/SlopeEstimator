package skarpa;

import java.util.ArrayList;
import static skarpa.Model.*;

public class Line {
	ArrayList<Integer> conCyc = new ArrayList<>();
	int a, b;
	
	private Line(int a, int b){
		this.a = a;
		this.b = b;
		for(int i : vertices[a].conCyc){ //dodaje info o linii we wspolnych cyklach
			if(vertices[b].conCyc.contains(i)){
				conCyc.add(i); 
			}	
		}	
	}
				
	int splitLine(int vx){ //metoda użyta dla linii - linia jest dzielona / zwraca nr nowej linii
		int temp = markerLT;
		
		lines[markerLT] = new Line(vx, b);
		this.b = vx;
		newMarkerL();
		for(int c : conCyc){ //dodaje do każdego cyklu info. o tworzonej linii
			cycles[c].lnList.add(temp); //dodać linę na odpowiednie miejsce w cyklu!!!
		}
		return temp;
	}
  	
	static int addLine(int vx){ //met. statyczna - dodaje linię do noworysowanego punktu
		int temp = markerLT;
		lines[markerLT] = new Line(activeVx, vx);
		vertices[activeVx].conLi.add(activeVx);
		tempCycle.lnList.add(activeVx);
		newMarkerL();
		return temp;
	}
	
	static int addLine(int a, int b){ //dodaje linię pomiedzy dwoam punktami (używane tylko przy usuwaniu vx powierzchniowych) i w kon Model
		int temp = markerLT;
		lines[markerLT] = new Line(a, b);
		newMarkerL();
		return temp;
	}

	static void deleteL(int ln){
		lines[ln] = null;
		if(ln < markerLT) markerLT = ln;
	}
}
