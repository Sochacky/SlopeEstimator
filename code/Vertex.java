package skarpa;

import java.util.ArrayList;
import java.util.HashSet;

import static skarpa.Model.*;

public class Vertex {
	boolean isSurface = false; 
	ArrayList<Integer> conCyc = new ArrayList<>();  //lista współtworzonych cykli
	ArrayList<Integer> conLi = new ArrayList<>();   //lista współtworzonych linii 
	ArrayList<Integer> conVx = new ArrayList<>();  //lista punktów połączonych z bieżącym
	private static HashSet<Integer> asmL = new HashSet<>(); //lista linii złączonych cykli
	private static ArrayList<Integer> asmC = new ArrayList<>(); //lista z połączonymi w kolejności vx cykli
	int x, y;  //współrzędne
	
	Vertex(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	private Vertex(int v){  //wsp. i wierzchołek z którego wychodzi
		x=drawX;
		y=drawY;
		conVx.add(v);
		if(!activeCyc.isEmpty())
			conCyc.add(activeCyc.get(0)); //?
	}
	
	private Vertex(Line ln){ //tworzy punkt na linii
		if(vertices[ln.a].isSurface==true && vertices[ln.b].isSurface==true)
			isSurface = true;
		x=drawX;
		y=drawY;
		conVx.add(ln.a); //dodaje do wierzchołka dowiązane wierzchołki
		conVx.add(ln.b);
		conCyc = ln.conCyc;
	}
	
	static int createVxOnPlane(int v){ //dodaniepunktu na pustce / zwraca nr punktu
		int temp = markerVT;
		int newLNum;
		vertices[markerVT] = new Vertex(v);
		newLNum = Line.addLine(markerVT);
		vertices[markerVT].conLi.add(newLNum);
		tempCycle.addVx(markerVT);
		newMarkerV();
		return temp;		
	}
	
	static int createVxOnLine(int l){ //tworzy i dodaje punkt na linii; zwraca nr punktu w vertices 
		Line ln = lines[l];
		int temp = markerVT;
		int aIndex, bIndex, newLNum;
		
		vertices[markerVT] = new Vertex(ln);
		vertices[markerVT].conLi.add(l); //dodaje 1szą linię do conLi nowego punktu
		newLNum = ln.splitLine(markerVT); //dzieli linie na dwie (ln zostaje obcięte do a-vx)
		vertices[markerVT].conLi.add(newLNum); //dodaje 2gą linę do conLi nowego punktu
		
		for(int c : ln.conCyc){ //dodaj vx do vxList każdego cyklu w odpowiednie miejsce
			aIndex = cycles[c].vxList.indexOf(ln.a);
			bIndex = cycles[c].vxList.indexOf(ln.b);
			if(aIndex < bIndex) cycles[c].addVx(bIndex, markerVT); //wstawiaj (vertices)[markerVT] pomiędzy punkty cyklu
			else cycles[c].addVx(aIndex, markerVT);
			cycles[c].lnList.add(l);
		}
		
		if(Events.drawMode){
			vertices[activeVx].conVx.add(markerVT); //dodaje info o vx w activeVx
			vertices[activeVx].conLi.add(Line.addLine(markerVT)); //dodaje linie miedzy tworzonym vx a vx na linii(konczacym tempCycle)
		}
		newMarkerV();
		
		return temp;
		//pododawać info o vx do wspoltworzonych cykli
	}
	
	static int createVxOnLineAsEnd(int l){
		int numV = createVxOnLine(l);
		int numL = Line.addLine(activeVx, numV);
		vertices[activeVx].conLi.add(numL);
		vertices[numV].conLi.add(numL);
		return numV;
	}
	
	static void reorganizeC(int v){
		ArrayList<Integer> temp;
		for(int i : vertices[v].conCyc){ //przeorganizuj cykle aby kończyły się na v
			temp = new ArrayList<>();
			temp.addAll(cycles[i].vxList.subList(cycles[i].vxList.indexOf(v)+1, cycles[i].vxList.size())); //od n+1 do końca
			temp.addAll(cycles[i].vxList.subList(0, cycles[i].vxList.indexOf(v)+1));
			cycles[i].vxList = temp;
		}
	}
	
	private static void delVx(int v){
		int lNum = -1;	
		
		if(vertices[v].isSurface){ //zakładam, że w przypadku vx pow. dowiązane pow. to conVx[0] i conVx[1]
			int a = vertices[v].conVx.get(0);
			int b = vertices[v].conVx.get(1);
			lNum = Line.addLine(a, b); //dodać linę do cyklu!!
			//usuwa z vxList a i b dowiązania do tego punktu i zastepuje je dow do siebie
			if(vertices[a].conVx.get(0) == v) vertices[a].conVx.set(0, b);
			else vertices[a].conVx.set(1, b);
			if(vertices[b].conVx.get(0) == v) vertices[b].conVx.set(0, a);
			else vertices[b].conVx.set(1, a);
			for(int c : vertices[v].conCyc){ //dodaje do cyklu powierzchniowego b (żeby tworzony cykl się zamknął)
				if(vertices[a].conCyc.contains(c)){
					if(cycles[c].vxList.get(1) == a) cycles[c].vxList.add(1, b);
					else cycles[c].vxList.add(cycles[c].vxList.size()-2, b);
				}
			}
			vertices[v].isSurface = false;
			delVx(v);
		}else{ //jeżeli punkt nie jest powierzchniowy
			for(int i : vertices[v].conVx){ //usuwa z każdego dowiązanego vx info. o sobie
				vertices[i].conVx.remove((Integer) v);
				for(int j : vertices[i].conLi){ //usuwa linie z punktu a powiązanego z tym
					if(lines[j].a == v || lines[j].b == v){
						for(int c : lines[j].conCyc){
							cycles[c].lnList.remove((Integer) j);
						}
						Line.deleteL(j);
						vertices[i].conLi.remove((Integer) j);
					}		
				}
			}
			
			for(int i : vertices[v].conVx){ //jeżeli dowiązany vx ma mniej niż 2 dow. to jest usuwany
				if(vertices[i].conVx.size() < 2) delVx(i);
			}
			
			for(int i : vertices[v].conCyc){ //usuwa info o sobie z wspóltw. cykli
				cycles[i].vxList.remove((Integer) v);
			}
			for(int i : vertices[v].conCyc){
				asmL.addAll(cycles[i].lnList);
			}
			if(lNum != -1) asmL.add(lNum);
			vertices[v] = null;
			if(v < markerVT) markerVT = v;
		}
	}
	
	private static void assemblyC(ArrayList<Integer> cList){ //"spaja" listy wszystkich cykli w jedną listę ułożoną w kolejności
		asmC.addAll(cycles[cList.get(0)].vxList);
		cList.remove(0);
		do{
			for(int i : cList){
				if(asmC.get(asmC.size()-1) == cycles[i].vxList.get(0)){ //jeżeli ostatni == prerwszy
					asmC.addAll(cycles[i].vxList.subList(1, cycles[i].vxList.size()));
					cList.remove((Integer) i);
				}else if(asmC.get(asmC.size()-1) == cycles[i].vxList.get(cycles[i].vxList.size()-1)){ //jeżeli ostatni == ostatni
					for(int j=cycles[i].vxList.size()-2; j>0; j--){ //dodaje w odwróconej kolejności
						asmC.add(cycles[i].vxList.get(j));
					}
					cList.remove((Integer) i);
				}
			}
		}while(cList.size() > 0);
	}
	
	static void deleteVx(int v){
		ArrayList<Integer> al = new ArrayList<>(); //lista łączonych cykli
		al.addAll(vertices[v].conCyc); 
		reorganizeC(v);
		delVx(v); //zwraca nr dodanej linii jeżeli punkt był pow.
		assemblyC(al);
		cycles[al.get(0)].vxList.clear(); //przypisuje złożoną listę vx do "pierwszego" cyklu
		cycles[al.get(0)].vxList.addAll(asmC);
		cycles[al.get(0)].lnList.clear();
		cycles[al.get(0)].lnList.addAll(asmL);
		al.remove(0); //czy to usuwa z indexu??!!
		for(int i : al){
			Cycle.deleteC(i);
		}
		asmC.clear();
		asmL.clear();
	}
}
