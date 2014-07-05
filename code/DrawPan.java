package skarpa;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;

import static skarpa.Model.*;

public class DrawPan extends Panel{
	//Model model;
	Events ev;
	
	public DrawPan(){
		setSize(600, 800);
		ev = new Events();
		addMouseMotionListener(ev);
		addMouseListener(ev);
		//model = new Model();
		setVisible(true);
		}
	
	public void paint(Graphics g){
		g.setColor(Color.red);
		for(int i=0; i<lines.length; i++){
			if(lines[i] != null)
				g.drawLine(vertices[lines[i].a].x, vertices[lines[i].a].y, 
						vertices[lines[i].b].x, vertices[lines[i].b].y);
		}
		for(int i=0; i<vertices.length; i++){
			if(vertices[i] != null)
				g.fillOval(vertices[i].x-3, vertices[i].y-3, 7, 7);
		}
		if(drawX!=-1&&drawY!=-1){
			if(activeVx != -1) g.drawLine(vertices[activeVx].x, vertices[activeVx].y, drawX, drawY);
			g.setColor(Color.green);
			g.fillOval(drawX-4, drawY-4, 9, 9);
		}
		/////////////////////////////////////////////////////////////////////////////////////
		g.setColor(Color.red);
		if(activeCyc.size() == 0){
			g.drawString("activeCyc = null", 20, 20);
		}else{
			for(int i : activeCyc){
				g.drawString("activeCyc = "+i, 20*i+20, 20);
			}
		}
		g.drawString("activeVx = "+activeVx, 20, 30);
		for(int i : tempCycle.vxList){
			g.drawString("tempCycle.vxList = ", 20, 40);
			g.drawString(""+i, 110+17*i, 40);
		}
		if(tempCycle.vxList.isEmpty()) g.drawString("tempCycle.vxList = empty", 20, 40);
		g.drawString("tempVxNum = "+ev.tempVxNum, 20, 50);
		g.drawString("drawMode = "+ev.drawMode, 20, 60);
		g.drawString(""+drawX, 20, 70);
		g.drawString(""+drawY, 50, 70);
		g.drawString("tempLine "+ev.tempLine, 20, 80);
		g.drawString(ev.tempLineSet.toString(), 20, 90);
		/////////////////////////////////////////////////////////////////////////////////////
	}
		
	public static void main(String[] arg0){
		DrawPan drawingPanel = new DrawPan();
		Frame win = new Frame("Moje Okno");
		win.add(drawingPanel);
		win.pack();
		win.setBounds(200, 100, 800, 600);
		win.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent we){
                System.exit(0);
            }
        });
		win.setVisible(true);
	}

	class Events extends MouseAdapter implements MouseMotionListener{
		final int BIG = 15;
		final int SMALL = 9;
		final int POINT = 1, LINE = 2, FREE = 3;
		int modeOfPoint; //1 punkt na linii 2 punkt na punkcie 3 punkt swobodny
		int tempLine;  //nr linii na której wypada drawXY
		int tempVxNum; //numer wierzchołka na którym jest kursor
		float n;
		boolean drawMode = false;
		private boolean draggedOnPoint;
		Point tempPoint = new Point(-1, -1);
		private HashSet<Integer> tempVxSet = new HashSet<>(); //przechowuje listę punktów z aktywnych cykli
		HashSet<Integer> tempLineSet = new HashSet<>(); //przechowuje listę linii z aktywnych cykli 
		
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
		
		void splitCycle(){ //cycles[activeCycle] jest "przycinany" 
			Cycle oldC;
			if(activeCyc.size() > 1){ //jeżeli punkt kończący jest drugim activeCyc >1
				for(int i : vertices[activeVx].conCyc){
					if(vertices[tempVxNum].conCyc.contains(i)){
						activeCyc = new ArrayList<>();
						activeCyc.add(i);
						break;
					}
				}
			}
			
			oldC = cycles[activeCyc.get(0)];
			
			int cut1 = oldC.vxList.indexOf(tempCycle.vxList.get(0)); //indeks w activeCyc.vxList punktu tempCycle.vxList[0]
			int cut2 = oldC.vxList.indexOf(tempCycle.vxList.get(tempCycle.vxList.size()-1));
			ArrayList<Integer> c = new ArrayList<>(oldC.vxList.subList(cut1+1, oldC.vxList.size()));
			c.addAll(oldC.vxList.subList(0, cut2));
			ArrayList<Integer> z = new ArrayList<>(oldC.vxList.subList(cut2+1, cut1));
			ArrayList<Integer> w = tempCycle.vxList;
			ArrayList<Integer> cl = new ArrayList<>(oldC.lnList.subList(cut1+1, oldC.lnList.size()));
			cl.addAll(oldC.lnList.subList(0, cut2));
			ArrayList<Integer> zl = new ArrayList<>(oldC.lnList.subList(cut2+1, cut1));
			ArrayList<Integer> wl = tempCycle.lnList;
			
			if(cut1<cut2){ 
				oldC.vxList.clear();
				oldC.vxList.addAll(c);
				oldC.vxList.addAll(w);
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
			float x1 = vertices[l.a].x,
				y1 = vertices[l.a].y,
				x2 = vertices[l.b].x,
				y2 = vertices[l.b].y,
				x3 = mouseX,
				y3 = mouseY,
				x4, y4,
				x, y;
			if(x2 == x1){
				x4 = mouseX +10;
				y4 = mouseY;
			}else if(y2 == y1){
				x4 = mouseX;
				y4 = mouseY + 10;
			}else{
				x4 = mouseX + 10;
				y4 = mouseY + (-(1/(y2-y1)/(x2-x1))*10); 
			}
				
			float delta = (x2-x1)*(y3-y4)-(x3-x4)*(y2-y1);
			float deltaNi = (x3-x1)*(y3-y4)-(x3-x4)*(y3-y1);
			float ni = deltaNi/delta; //jeżeli odcinki są równoległe ni = infinity || NaN
			x = (1-ni)*x1+ni*x2;
			y = (1-ni)*y1+ni*y2;
			if(ni<1 && ni>0 && (Math.abs(x-x3)*Math.abs(x-x3)+Math.abs(y-y3)*Math.abs(y-y3)) < Math.pow((SMALL-1)/2, 2)){ //POTĘGA ODLEGŁOŚCI OD LINII!!!??
				return new Point((int)x, (int)y);
			}else return new Point(-1, -1);
		}
		
		public void mouseMoved(MouseEvent e){
			tempPoint.setLocation(-1, -1);
			tempVxSet.clear();
			tempLineSet.clear();
			mouseX = e.getX();
			mouseY = e.getY();
			tempVxNum = -1;	
			
			if(drawMode){ 				
				for(int i : activeCyc){ //ustawienie zbiorów punktów i linii w aktywnych cyklach
					tempVxSet.addAll(cycles[i].vxList);
					tempLineSet.addAll(cycles[i].lnList);
				}
				tempVxSet.addAll(tempCycle.vxList);
				tempLineSet.addAll(tempCycle.lnList);
				
				for(int i : tempLineSet){ //sprawdzaj przecięcie linii 
					if(lines[i] == null) continue;
					int x1 = vertices[lines[i].a].x, 
						y1 = vertices[lines[i].a].y,
						x2 = vertices[lines[i].b].x, 
						y2 = vertices[lines[i].b].y,
						x3 = vertices[activeVx].x,
						y3 = vertices[activeVx].y;
					if((n = cross(x1, y1, x2, y2, x3, y3, mouseX, mouseY))>0){
						int x = (int) ((1-n)*vertices[activeVx].x+n*mouseX);
						int y = (int) ((1-n)*vertices[activeVx].y+n*mouseY);
						if(Math.abs(x3-x) < Math.abs(x3-tempPoint.getX())){ //czy dX jest mniejszy
							tempPoint.setLocation(x, y);
							tempLine = i;
						}else if(Math.abs(x3-x) == Math.abs(x3-tempPoint.getX())){
							if(Math.abs(y3-y) < Math.abs(y3-tempPoint.getY())) //czy dY jest mniejszy
								tempPoint.setLocation(x, y);
								tempLine = i;
						}
					}
				}
				if(tempPoint.getX() != -1 && tempPoint.getY() != -1){ //jeżeli przecina linię
					drawX = (int) tempPoint.getX();
					drawY = (int) tempPoint.getY();
					
					if(drawX<vertices[lines[tempLine].a].x+8 && //jezeli znajduje sie w poblizu punktu a linii
							drawX>vertices[lines[tempLine].a].x-8 && 
							drawY<vertices[lines[tempLine].a].y+8 && 
							drawY>vertices[lines[tempLine].a].y-8){
						drawX = vertices[lines[tempLine].a].x;
						drawY = vertices[lines[tempLine].a].y;
						modeOfPoint = POINT;
						tempVxNum = lines[tempLine].a;
						repaint();
						return;
					}else if(drawX<vertices[lines[tempLine].b].x+8 && //jezeli znajduje sie w poblizu punktu b linii
							drawX>vertices[lines[tempLine].b].x-8 && 
							drawY<vertices[lines[tempLine].b].y+8 && 
							drawY>vertices[lines[tempLine].b].y-8){
						drawX = vertices[lines[tempLine].b].x;
						drawY = vertices[lines[tempLine].b].y;
						modeOfPoint = POINT;
						tempVxNum = lines[tempLine].b;
						repaint();
						return;
					}
					modeOfPoint = LINE;
				}else{ //jeżeli nie przecina linii
					drawX = mouseX;
					drawY = mouseY;
					for(int i : tempVxSet){ //sprawdzaj bliskość punktów
						if(drawX<vertices[i].x+8 && drawX>vertices[i].x-8 &&
								drawY<vertices[i].y+8 && drawY>vertices[i].y-8){
							drawX = vertices[i].x;
							drawY = vertices[i].y;
							tempVxNum = i;
							tempLine = -1;
							repaint();
							return;
						}
					}
					for(int i : tempLineSet){ //sprawdzaj bliskość do linii
						if(lines[i] == null) continue;
						Point point = vicinity(lines[i]);
						if(point.getX() != -1 && point.getY() != -1){
							drawX = (int) point.getX();
							drawY = (int) point.getY();
							modeOfPoint = LINE;
							tempLine = i;
							repaint();
							return; 
						}
					}
				}
				//tempVxNum = -1;
				//tempLine = -1;
				modeOfPoint = FREE;
				repaint();				
		    }else{ // !drawMode
	    		for(int i=0; i<vertices.length; i++){  //sprawdzanie punktów
					if(vertices[i] == null) continue;
					if(mouseX>vertices[i].x-8 && mouseX<vertices[i].x+8 && //jeżeli mouseXY znajduje sie w pob. punktu
							mouseY>vertices[i].y-8 && mouseY<vertices[i].y+8){
						drawX = vertices[i].x;
						drawY = vertices[i].y;
						modeOfPoint = POINT;
						tempVxNum = i; //podaje nr punktu
						repaint();
						return;
					}
				}
	    		Point proxy;
	    		for(int j=0; j<lines.length; j++){  //sprawdzanie linii i rys na linii jeżeli w pobliżu
					if(lines[j] == null) continue;
	    			proxy = vicinity(lines[j]);
	    			if(proxy.getX() != -1 &&
	    					proxy.getY() != -1){
	    				drawX = (int) proxy.getX();
	    				drawY = (int) proxy.getY();
	    				modeOfPoint = LINE;
	    				tempLine = j; //podaje nr linii
	    				repaint();
	    				return;
	    			}
				}
	    		drawX = -1;
				drawY = -1;
				modeOfPoint = FREE;
				repaint();
	    	}
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
				
		public void mouseReleased(MouseEvent e){
			if(drawMode){ //dodaj punkt i jeżeli wypada on na punkcie/linii zakończ rysowanie (chyba że wypada na swoim punkcie/ linii)
				
				switch(modeOfPoint){
					case POINT:
						if(!tempCycle.vxList.contains(tempVxNum)){
							tempCycle.addVx(tempVxNum);
							splitCycle();
							addCycle();
							activeVx = -1;
							activeCyc.clear();
							drawMode = false;
							repaint();
						}
						break;
					case LINE:
						if(!tempCycle.lnList.contains(tempLine)){
							tempVxNum = Vertex.createVxOnLineAsEnd(tempLine);
							tempCycle.addVx(tempVxNum); //?????????????
							splitCycle();
							addCycle();
							activeVx = -1;
							activeCyc.clear();
							drawMode = false;
							repaint();
						}
						break;
					case FREE:
						 int num = Vertex.createVxOnPlane(activeVx);//CZY TO NIE JEST GŁUPIE? CHYBA NIE...
						 activeVx = num;
						if(activeCyc.size() > 1){ //jeżeli w trybie rysowania jest więcej aktywnych cykli, drugi punkt ogranicza do jednego ak. cyklu
							for(int i : activeCyc){
								if(isInCycle(drawX, drawY, cycles[i])){
									activeCyc = new ArrayList<>();
									activeCyc.add(i);
									break;
								}
							}
						}
						repaint();
						break;
				}
			}else{ //!drawMode - dodaj punkt jeżeli wypada na punkcie/linii i rozpocznij rysowanie
				switch(modeOfPoint){
				case POINT:
					//jeżeli punkt nie był przesuwany
					if(!draggedOnPoint){
						activeCyc = vertices[tempVxNum].conCyc; //ogranicz aCykle do tych zwiazanych z vx
						tempCycle.addVx(tempVxNum);
						activeVx = tempVxNum;
						drawMode = true;
						drawX = vertices[tempVxNum].x;
						drawY = vertices[tempVxNum].y;
						repaint();
					}else draggedOnPoint = false; //był przesuwany
					break;
				case LINE:
					activeVx = Vertex.createVxOnLine(tempLine);
					tempCycle.addVx(activeVx);
					activeCyc = lines[tempLine].conCyc;
					drawMode = true;
					repaint();
					break;
				}
			}
		}
		
		public void mouseClicked(MouseEvent e){
			/*if(drawMode){
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
			}*/
		}

		//public void mousePressed(){}
	}
}

