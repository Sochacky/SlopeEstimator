package skarpa;

import java.util.ArrayList;

public class Model{	
	static Line[] lines = new Line[20];
	static Cycle[] cycles = new Cycle[20];
	static Vertex[] vertices = new Vertex[20];
	static Cycle tempCycle = new Cycle();
	static int activeVx = -1; //nr wierzchołka z którego wychodzi rysowana linia
	static int markerCT = 0, markerLT = 0, markerVT = 0;
	static int maxH;
	static int drawX = -1, drawY = -1; //ma zostać przekazane do met. paint()
	static int mouseX, mouseY;
	static ArrayList<Integer> activeCyc = new ArrayList<>();
	
	static{
		maxH = 1000;
		/*twórz kwadratową bryłę o wymiarach 
		 * zadanych przez użytkownika 
		 */
		//dodaj vx
		vertices[markerVT] = new Vertex(50, 500); //0
		vertices[markerVT].conCyc.add(0);
		vertices[markerVT].conVx.add(2);
		vertices[markerVT].conVx.add(1);
		newMarkerV();
		vertices[markerVT] = new Vertex(600, 500); //1
		vertices[markerVT].conCyc.add(0);
		vertices[markerVT].conCyc.add(1);
		vertices[markerVT].conVx.add(3);
		vertices[markerVT].conVx.add(0);
		vertices[markerVT].conVx.add(4);
		newMarkerV();
		vertices[markerVT] = new Vertex(50, 300); //2
		vertices[markerVT].conCyc.add(0);
		vertices[markerVT].conVx.add(3);
		vertices[markerVT].conVx.add(0);
		//vertices[markerVT].isSurface = true;
		newMarkerV();
		vertices[markerVT] = new Vertex(600, 150); //3
		vertices[markerVT].conCyc.add(0);
		vertices[markerVT].conCyc.add(1);
		vertices[markerVT].conVx.add(2);
		vertices[markerVT].conVx.add(1);
		vertices[markerVT].conVx.add(5);
		//vertices[markerVT].isSurface = true;
		newMarkerV();
		tempCycle.vxList.add(0);
		tempCycle.vxList.add(1);
		tempCycle.vxList.add(2);
		tempCycle.vxList.add(3);
		//dodaj line do cyklu
		tempCycle.lnList.add(Line.addLine(tempCycle.vxList.get(0), tempCycle.vxList.get(1)));
		tempCycle.lnList.add(Line.addLine(tempCycle.vxList.get(1), tempCycle.vxList.get(3)));
		tempCycle.lnList.add(Line.addLine(tempCycle.vxList.get(2), tempCycle.vxList.get(3)));
		tempCycle.lnList.add(Line.addLine(tempCycle.vxList.get(2), tempCycle.vxList.get(0)));
		//dodaj punktom linie
		vertices[0].conLi.add(0);
		vertices[0].conLi.add(3);
		vertices[1].conLi.add(0);
		vertices[1].conLi.add(1);
		vertices[2].conLi.add(2);
		vertices[2].conLi.add(3);
		vertices[3].conLi.add(1);
		vertices[3].conLi.add(2);
		addCycle();
		
		/*vertices[markerVT] = new Vertex(50, 500); //4
		vertices[markerVT].conCyc.add(1);
		vertices[markerVT].conVx.add(5);
		vertices[markerVT].conVx.add(1);
		newMarkerV();
		vertices[markerVT] = new Vertex(50, 500); //5
		vertices[markerVT].conCyc.add(1);
		vertices[markerVT].conVx.add(3);
		vertices[markerVT].conVx.add(4);
		newMarkerV();
		
		tempCycle.vxList.add(1);
		tempCycle.vxList.add(4);
		tempCycle.vxList.add(5);
		tempCycle.vxList.add(3);
		
		tempCycle.lnList.add(Line.addLine(tempCycle.vxList.get(4), tempCycle.vxList.get(1)));
		tempCycle.lnList.add(Line.addLine(tempCycle.vxList.get(3), tempCycle.vxList.get(5)));
		tempCycle.lnList.add(Line.addLine(tempCycle.vxList.get(5), tempCycle.vxList.get(4)));
		tempCycle.lnList.add(1);
		
		vertices[1].conLi.add(4);
		vertices[3].conLi.add(5);
		vertices[5].conLi.add(5);
		vertices[5].conLi.add(6);
		vertices[4].conLi.add(6);
		vertices[4].conLi.add(4);
		addCycle();*/
	}
	
	static void addCycle(){ //dodaje tempCycle do cycles[] i czyści tempCycle
		//dodac info o tym cyklu do wszystkich skladowych
		cycles[markerCT] = tempCycle;
		tempCycle = new Cycle();
		newMarkerC();
	}
	
	/*metody newMarkerT() powiększają daną tablicę
	 * jeżeli jest zapełniona i przesuwają marker
	 * na następne wolne miejsce
	 */
	static void newMarkerV(){
		int temp = markerVT;
		
		for(int i=markerVT+1; i<vertices.length; i++){
			if(vertices[i] == null){
				markerVT = i; 
				break;
			}
		}
		
		if(markerVT == temp){  //powiększenie tablicy w razie braku miejsca na następny wierzchołek
			Vertex[] tempVx = new Vertex[vertices.length + 20];
			for(int i=0; i<vertices.length; i++){
				tempVx[i] = vertices[i];
			}
			markerVT = vertices.length;
			vertices = tempVx;
		}
	}
	
	static void newMarkerL(){
		int temp = markerLT;
		
		for(int i=markerLT+1; i<lines.length; i++){
			if(lines[i] == null){
				markerLT = i; 
				break;
			}
		}
		
		if(markerLT == temp){  //powiększenie tablicy w razie braku miejsca na następny wierzchołek
			Line[] tempLn = new Line[lines.length + 20];
			for(int i=0; i<lines.length; i++){
				tempLn[i] = lines[i];
			}
			markerLT = lines.length;
			lines = tempLn;
		}
	}
	
	static void newMarkerC(){
		int temp = markerCT;
		
		for(int i=markerCT+1; i<cycles.length; i++){
			if(cycles[i] == null){
				markerCT = i; 
				break;
			}
		}
		
		if(markerCT == temp){  //powiększenie tablicy w razie braku miejsca na następny wierzchołek
			Cycle[] tempCs = new Cycle[cycles.length + 20];
			for(int i=0; i<cycles.length; i++){
				tempCs[i] = cycles[i];
			}
			markerCT = cycles.length;
			cycles = tempCs;
		}
	}	
}
