package skarpa;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;

import static skarpa.Model.*;

class Events extends MouseAdapter implements MouseMotionListener{
	final int BIG = 9;
	final int SMALL = 5;
	final int POINT = 1, LINE = 2, FREE = 3;
	int modeOfPoint; //1 punkt na linii 2 punkt na punkcie 3 punkt swobodny
	int tempLine;  //nr linii na której wypada drawXY
	int tempVxNum; //numer wierzchołka na którym jest kursor
	float n;
	static boolean drawMode = false;
	private boolean draggedOnPoint;
	Point tempPoint = new Point(-1, -1);
	private HashSet<Integer> tempVxSet = new HashSet<>(); //przechowuje listę punktów z aktywnych cykli
	private HashSet<Integer> tempLineSet = new HashSet<>(); //przechowuje listę linii z aktywnych cykli 
	
	boolean isInCycle(int theX, int theY, Cycle c){
		int count = 0;
		int detVirt;
		int j;
		Integer[] l = new Integer[c.vxList.size()];
		c.vxList.toArray(l);
		for(int i=1; i<l.length; i++){
			j = (i==l.length-1 ? 0: i);
			detVirt = det(vertices[l[i]].x, 
					vertices[l[i-1]].y, 
					vertices[l[i]].x, 
					vertices[l[i]].y, 
					theX, 
					theY);
			if(detVirt == 0){
				if(Math.signum(det(vertices[l[i-1]].x,  
						vertices[l[i-1]].y, 
						vertices[l[i]].x, 
						vertices[l[i]].y, 
						vertices[l[j]].x, 
						vertices[l[j]].y)) ==
						Math.signum(det(vertices[l[i-1]].x, 
								vertices[l[i-1]].y, 
								vertices[l[i]].x, 
								vertices[l[i]].y, 
								vertices[l[i-2]].x, 
								vertices[l[i-2]].y)))
					count++;
			}else if(Math.signum(detVirt) !=
					Math.signum(det(vertices[l[i]].x, 
							vertices[l[i]].y, 
							vertices[l[i+1]].x, 
							vertices[l[i+1]].y, 
							theX, 
							maxH)))
				count++;
		}
		if(count%2!=0){
			return true;
		}else return false;
	}
	
	void splitCycle(){ //cycles[activeCycle] jest "przycinany" !!!!!!???????!!!!!
		Cycle oldC = cycles[activeCyc.get(0)];
		int cut1 = oldC.vxList.indexOf(tempCycle.vxList.get(0)); //indeks w activeCyc.vxList punktu tempCycle.vxList[0]
		int cut2 = oldC.vxList.indexOf(tempCycle.vxList.get(tempCycle.vxList.size()-1));
		ArrayList<Integer> c = (ArrayList<Integer>) oldC.vxList.subList(cut1+1, oldC.vxList.size());
		c.addAll(oldC.vxList.subList(0, cut2));
		ArrayList<Integer> z = (ArrayList<Integer>) oldC.vxList.subList(cut2+1, cut1);
		ArrayList<Integer> w = tempCycle.vxList;
		ArrayList<Integer> cl = (ArrayList<Integer>) oldC.lnList.subList(cut1+1, oldC.lnList.size());
		cl.addAll(oldC.lnList.subList(0, cut2));
		ArrayList<Integer> zl = (ArrayList<Integer>) oldC.lnList.subList(cut2+1, cut1);
		ArrayList<Integer> wl = tempCycle.lnList;
		
		if(cut1<cut2){ 
			oldC.vxList.clear();
			oldC.vxList.addAll(c);
			oldC.vxList.addAll(w);
			/*for(int i : w){
				oldC.vxList.add(0, i);
				vertices[i].conCyc.add(markerCT);
			}*/
			tempCycle.vxList.clear();
			tempCycle.vxList.addAll(z);
			for(int i : w){
				tempCycle.vxList.add(0, i);
				vertices[i].conCyc.add(markerCT);
			}
			for(int i : z){ //ustawia nr nowego cyklu na miejscu starego
				vertices[i].conCyc.set(vertices[i].conCyc.indexOf(vertices[i].conCyc), markerCT); 
			}
			//linie
			oldC.lnList.clear();
			oldC.lnList.addAll(cl);
			oldC.lnList.addAll(wl);
			tempCycle.lnList.clear();
			tempCycle.lnList.addAll(zl);
			for(int i : wl){
				tempCycle.lnList.add(0, i);
				lines[i].conCyc.add(markerCT);
			}
		}else{ 
			//wierzchołki
			oldC.vxList.clear();
			oldC.vxList.addAll(c);
			for(int i : w){
				oldC.vxList.add(0, i);
				vertices[i].conCyc.add(markerCT);
			}
			tempCycle.vxList.clear();
			tempCycle.vxList.addAll(z);
			tempCycle.vxList.addAll(w);
			for(int i : z){ //ustawia nr nowego cyklu na miejscu starego
				vertices[i].conCyc.set(vertices[i].conCyc.indexOf(vertices[i].conCyc), markerCT); 
			}
			//linie
			oldC.lnList.clear();
			oldC.lnList.addAll(cl);
			for(int i : wl){
				oldC.lnList.add(0, i);
				lines[i].conCyc.add(markerCT);
			}
			tempCycle.lnList.clear();
			tempCycle.lnList.addAll(zl);
			tempCycle.lnList.addAll(wl);
		}
		addCycle();				
	}
	
	public Events(){
		
	}
	
	private int det(int x1, int y1, int x2, int y2, int x3, int y3){
		return x1*y2+x2*y3+x3*y1-y1*x2-y2*x3-y3*x1;
	}
	
	private float cross(int x1, int y1, 
			int x2, int y2, 
			int x3, int y3, 
			int x4, int y4){ //jeżeli zwracane ni jest wieksze od 0 to linie sie przecinają
		
	float delta = (x2-x1)*(y3-y4)-(x3-x4)*(y2-y1);
	float deltaNi = (x3-x1)*(y3-y4)-(x3-x4)*(y3-y1);
	float ni = deltaNi/delta; //jeżeli odcinki są równoległe ni = inf. || NaN
	
	if(ni<1&&ni>0){
		delta = (x4-x3)*(y1-y2)-(x1-x2)*(y4-y3);
		deltaNi = (x1-x3)*(y1-y2)-(x1-x3)*(y1-y3);
		
		ni = deltaNi/delta; //jeżeli odcinki są równoległe ni = inf.(jeżeli sie pokrywają) || NaN (jeżeli sie nie pokrywają)			
		if(ni<1&&ni>0){
			return ni;
		}else return -1;
	}else return -1;
}
	
	private Point vicinity(Line l){ //return value of crossing point if mouse is near enough
		int x1 = vertices[l.a].x,
			y1 = vertices[l.a].y,
			x2 = vertices[l.b].x,
			y2 = vertices[l.b].y,
			x3 = mouseX,
			y3 = mouseY,
			x4 = mouseX + 10,
			y4,
			x, y;
		try{
			y4 = mouseY + (-1/(y2-y1)/(x2-x1)*10); //dzielenie przez 0
		}catch(ArithmeticException e){ y4 = mouseY; }
			
		float delta = (x2-x1)*(y3-y4)-(x3-x4)*(y2-y1);
		float deltaNi = (x3-x1)*(y3-y4)-(x3-x4)*(y3-y1);
		float ni = deltaNi/delta; //jeżeli odcinki są równoległe ni = inf. || NaN
		x = (int)((1-ni)*x1+ni*x2);
		y = (int)((1-ni)*y1+ni*y2);
		if(ni<1 && ni>0 && (x*x+y*y) < Math.pow((SMALL-1)/2, 2)){ //POTĘGA ODLEGŁOŚCI OD LINII!!!??
			return new Point(x, y);
		}else return new Point(-1, -1);
	}
	
	public void mouseMoved(MouseEvent e){
		mouseX = e.getX();
		mouseY = e.getY();
		first: if(drawMode){ 
			
			/*SPRAWDZAĆ CZY JEŻELI RYSUJEMY OD VX.SURFACE TO MOUSEXY ZNAJDUJE SIĘ W OBRĘBIE AKTYWNYCH CYKLI*/
			if(vertices[activeVx].isSurface){ //jeżeli vx z którego rysujemy jest pow.
				int x1 = vertices[activeVx].x, 
					y1 = vertices[activeVx].y,
					x2 = vertices[vertices[activeVx].conVx.get(0)].x, 
					y2 = vertices[vertices[activeVx].conVx.get(0)].y;
				if(x1 < x2){ //jeżeli 0.pow jest na prawo
					if(det(x1, y1, x2, y2, mouseX, mouseY)>0){
						drawX = x1;
						drawY = y1;
						break first;
					}
				}else{ //jeżeli jest na lewo
					if(det(x1, y1, x2, y2, mouseX, mouseY)<0){
						drawX = x1;
						drawY = y1;
						break first;
					}
				}
			}
			
			for(int i : activeCyc){ //ustawienie zbiorów punktów i linii w aktywnych cyklach
				tempVxSet.addAll(cycles[i].vxList);
				tempLineSet.addAll(cycles[i].lnList);
			}
			
			for(int i : tempLineSet){ //sprawdzaj przecięcie linii 
				if((n = cross(vertices[lines[i].a].x, vertices[lines[i].a].y, 
						vertices[lines[i].b].x, vertices[lines[i].b].y, 
	    				vertices[activeVx].x, vertices[activeVx].y,
	    				mouseX, mouseY))>0){
					int x = (int) ((1-n)*vertices[activeVx].x+n*mouseX);
					int y = (int) ((1-n)*vertices[activeVx].y+n*mouseY);
					if(Math.abs(vertices[activeVx].x-x) < Math.abs(vertices[activeVx].x-tempPoint.getX())){ //czy dX jest mniejszy
						tempPoint.setLocation(x, y);
						tempLine = i;
					}else if(Math.abs(vertices[activeVx].x-x) == Math.abs(vertices[activeVx].x-tempPoint.getX())){
						if(Math.abs(vertices[activeVx].y-y) < Math.abs(vertices[activeVx].y-tempPoint.getY())) //czydY jest mniejszy
							tempPoint.setLocation(x, y);
							tempLine = i;
					}
				}
			}
			
			if(tempPoint.getX() == -1 && tempPoint.getY() == -1){ //jeżeli nie przecina żadnej linii
				for(int i : tempVxSet){ //sprawdzaj bliskość punktów
					if(drawX<vertices[i].x+8 && drawX>vertices[i].x-8 &&
							drawY<vertices[i].y+8 && drawY>vertices[i].y-8){
						drawX = vertices[i].x;
						drawY = vertices[i].y;
						tempVxNum = i;
						break first;
					}
				}
			}else{ //jeżeli przecina linę
				//sprawdź czy punkt rysowany nie jest w pobliżu wierzchołków A lub B danej linii
				if(tempPoint.getX() < vertices[lines[tempLine].a].x+8 && 
						tempPoint.getX() > vertices[lines[tempLine].a].x-8 &&
						tempPoint.getY() < vertices[lines[tempLine].a].y+8 && 
						tempPoint.getY() > vertices[lines[tempLine].a].y-8){
					drawX = vertices[lines[tempLine].a].x;
					drawY = vertices[lines[tempLine].a].y;
					tempVxNum = lines[tempLine].a; //numer vx na którym jest rysowany punkt
					modeOfPoint = POINT; 
					break first;
				}else if(tempPoint.getX() < vertices[lines[tempLine].b].x+8 && 
						tempPoint.getX() > vertices[lines[tempLine].b].x-8 &&
						tempPoint.getY() < vertices[lines[tempLine].b].y+8 && 
						tempPoint.getY() > vertices[lines[tempLine].b].y-8){
					drawX = vertices[lines[tempLine].b].x;
					drawY = vertices[lines[tempLine].b].y;
					modeOfPoint = lines[tempLine].b;
					modeOfPoint = POINT; 
					break first;
				}
			}
			
			for(int i : tempLineSet){ //sprawdzaj bliskość do linii
				Point point = vicinity(lines[i]);
				if(point.getX() != -1 && point.getY() != -1){
					drawX = (int) point.getX();
					drawY = (int) point.getY();
					modeOfPoint = LINE;
					break first; 
				}
			}					
			drawX = mouseX; //jeżeli nie znajduje sie w poblizu punktu/linii i nie przecina linii
			drawY = mouseY;
			modeOfPoint = FREE;
			
			
	    }else{ //jeżeli nie jest to tryb rysowania
    		for(int i=0; i<vertices.length; i++){  //sprawdzanie punktów
				if(vertices[i] != null){
					if(mouseX>vertices[i].x-4 && mouseX<vertices[i].x+4 && //jeżeli mouseXY znajduje sie w pob. punktu
							mouseY>vertices[i].y-4 && mouseY<vertices[i].y+4){
						drawX = mouseX;
						drawY = mouseY;
						//repaint
						return;
					}else{  
						for(int j=0; j<lines.length; j++){  //sprawdzanie linii i rys na linii jeżeli w pobliżu
							if(lines[j] == null) continue;
			    			Point proxy = vicinity(lines[j]);
			    			if(proxy.getX() != -1 &&
			    					proxy.getY() != -1){
			    				drawX = (int) proxy.getX();
			    				drawY = (int) proxy.getY();
			    				//repaint();
			    			}else{
			    				drawX = -1;
			    				drawY = -1;
			    			}
						}
					}
				}
				continue;
			}		    	   
    	}
		tempVxSet.clear();
		tempLineSet.clear();
		//tempPoint.setLocation(-1, -1);
	}
	
	public void mouseDragged(MouseEvent e){
		if(drawMode){
			mouseMoved(e); //??
		}else if(modeOfPoint == POINT){
			vertices[tempVxNum].x = drawX = mouseX;
			vertices[tempVxNum].y = drawX = mouseY;
			draggedOnPoint = true;
		}
	}
	
	//public void mousePressed(){}
	
	public void mouseReleased(MouseEvent e){
		if(drawMode){ //dodaj punkt i jeżeli wypada on na punkcie/linii zakończ rysowanie (chyba że wypada na swoim punkcie/ linii)
			
			switch(modeOfPoint){
				case POINT:
					if(!tempCycle.vxList.contains(tempVxNum)){
						//przerób oba cykle i drawMode = false
						splitCycle();
						addCycle();
						drawMode = false;
						//repaint
					}
					break;
				case LINE:
					if(!tempCycle.lnList.contains(tempLine)){
						/*dodaj na linii punkt i przerób cykle
						 * drawMode = false
						 */
						Vertex.createVxOnLine(tempLine);
						splitCycle();
						addCycle();
						drawMode = false;
						//repaint
					}
					break;
				case FREE:
					Vertex.createVxOnPlane(activeVx);
					if(activeCyc.size()>1){ //jeżeli w trybie rysowania jest więcej aktywnych cykli, drugi punkt ogranicza do jednego ak. cyklu
						for(int i : activeCyc){
							if(isInCycle(drawX, drawY, cycles[i])){
								activeCyc = new ArrayList<>();
								activeCyc.add(i);
								break;
							}
						}
					}
					break;
					//reapaint;
			}
		}else{ //dodaj punkt jeżeli wypada na punkcie/linii i rozpocznij rysowanie
			switch(modeOfPoint){
			case POINT:
				//jeżeli punkt nie był przesuwany
				if(!draggedOnPoint){
					activeCyc = vertices[tempVxNum].conCyc; //ogranicz aCykle do tych zwiazanych z vx
					activeVx = tempVxNum;
					drawMode = true;
				}else draggedOnPoint = false; //był przesuwany 
			case LINE:
				Vertex.createVxOnLine(tempLine);
				activeCyc = lines[tempLine].conCyc;
				drawMode = true;
			}
		}
	}
	
	/*public void mouseClicked(MouseEvent e){
		if(drawMode){
			switch(modeOfPoint){
			case POINT:
				if(!tempCycle.vxList.contains(tempVxNum)) //jeżeli punkt na którym znajduje sie kursor NIE nalezy do tempCycle
					//zamknij cykl
				break;
			case LINE:
				if
			case FREE:
				
				break;
			}
		}else{ //nie tryb rysowania
			switch(modeOfPoint){
			case POINT:
				
				break;
			case POINT:
				
			case FREE:
				break;
			}
		}
	}*/

}
