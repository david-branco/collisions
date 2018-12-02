import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import processing.core.*;


public class Arena extends PApplet{

	private Jogador j;

	private ArrayList<Corpo> obstaculos = new ArrayList<Corpo>();
	private HashMap<String,Jogador> jogadores = new HashMap<String,Jogador>();
	private ArrayList<Criatura> criaturas = new ArrayList<Criatura>();

	public Ligacao lig;


	public Arena (Ligacao lig, Jogador j ,ArrayList<Corpo> obs, ArrayList<Criatura> cs, HashMap<String,Jogador> jgdrs) {
		this.lig = lig;
		this.j = j;
		for(Corpo c : obs)
			this.obstaculos.add(c);
		for(Criatura cr : cs)
			this.criaturas.add(cr);
		for(Jogador jg : jgdrs.values())
			this.jogadores.put(jg.getName(), jg);



		Runnable r = new Runnable(){
			@Override
			public void run() {
				Arena arena = Arena.this;
				while(true){
					String res = arena.lig.read();
					if(res != null){
						StringTokenizer stk = new StringTokenizer(res," ");
						String token = stk.nextToken();

						if(token.equals("logged")){
							String nomej = stk.nextToken();
							int xj = (new Integer(stk.nextToken()).intValue());
							int yj = (new Integer(stk.nextToken()).intValue());
							int tamj = (new Integer(stk.nextToken()).intValue());
							int acelj = (new Integer(stk.nextToken()).intValue());
							int speedj = (new Integer(stk.nextToken()).intValue());
							int pontj = (new Integer(stk.nextToken()).intValue());

							Jogador jj = new Jogador(nomej,xj,yj,tamj,acelj,speedj,pontj);
							arena.jogadores.put(nomej, jj);

						}


						else if(token.equals("criatMoved")){
							int nc = (new Integer(stk.nextToken()).intValue());
							int xc = (new Integer(stk.nextToken()).intValue());
							int yc = (new Integer(stk.nextToken()).intValue());
							int tamc = (new Integer(stk.nextToken()).intValue());
							int speedc = (new Integer(stk.nextToken()).intValue());
							int bonusc = (new Integer(stk.nextToken()).intValue());
							int tipoc = (new Integer(stk.nextToken()).intValue());
							int xdirc = (new Integer(stk.nextToken()).intValue());
							int ydirc = (new Integer(stk.nextToken()).intValue());
							Criatura cr = new Criatura(nc,xc,yc,tamc,speedc,bonusc,tipoc,xdirc,ydirc);
							arena.criaturas.set(nc,cr);

						}
						else if(token.equals("jgMoved") || token.equals("jgUpdated")){
							String nome = stk.nextToken();
							int x = (new Integer(stk.nextToken()).intValue());
							int y = (new Integer(stk.nextToken()).intValue());
							int tam = (new Integer(stk.nextToken()).intValue());
							int acel = (new Integer(stk.nextToken()).intValue());
							int speed = (new Integer(stk.nextToken()).intValue());
							int pont = (new Integer(stk.nextToken()).intValue());

							if(nome.equals(arena.j.getName())) {
								arena.j.setX(x);arena.j.setY(y);arena.j.setTam(tam);
								arena.j.setAcel(acel);arena.j.setSpeed(speed);arena.j.setPont(pont);
							}
							else {
								Jogador jj = new Jogador(nome,x,y,tam,acel,speed,pont);
								arena.jogadores.put(nome, jj);
							}
						}
						else if(token.equals("logout")){
							String nome = stk.nextToken();
	                    	if(nome.equals(arena.j.getName())) {
	                    		arena.lig.disconnect();
	                    		try {
	    							Thread.sleep(2000);
	    						} catch (InterruptedException e) {
	    							e.printStackTrace();
	    						}
	                
	                    		arena.setVisible(false);
	                    		arena.dispose();
	                    		System.exit(0);	                    		
	                    	}
	                    	else 
	                    		arena.jogadores.remove(nome);
	                  
							}
					}
					else{
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("Posicao :" + arena.j.getX() + " " + arena.j.getY());
					}
				}
			}

		};
		(new Thread(r)).start();


	}

	public void displayCriat(Criatura c) {
		if(c.getTipo() == 1) this.fill(color(0,255,0));
		if(c.getTipo() == 2) this.fill(color(255,0,0));
		this.ellipse(c.getX(),c.getY(),c.getTam(),c.getTam());
		this.text(c.getNum(), c.getX(), c.getY()+c.getTam()+2);
	}

	public void displayObs(Corpo c) {
		this.fill(color(0,0,0));
		this.ellipse(c.getX(),c.getY(),c.getTam(),c.getTam());
		
	}

	public void displayJogador(Jogador j) {
		this.fill(0,0,255);
		this.ellipse(j.getX(),j.getY(),j.getTam(),j.getTam());
		this.text(j.getName(), j.getX(), j.getY()+j.getTam()+2);
	}


	public boolean colide(int x, int y, int tam, ArrayList<Corpo> corpos) {
		for(Corpo c : corpos) {
			double dist = Math.sqrt( Math.pow( ( (double) x - (double) c.getX() ),2.0) + Math.pow( ( (double) y - (double) c.getY() ),2.0) );
			if(dist <= tam/2 + c.getTam()/2) return true;
		}
		return false;
	}

	public void drive(Jogador j) {
		boolean mexeu = false;
		if(this.keyPressed) {
			mexeu = true;
			switch(this.keyCode) {
			case PConstants.RIGHT: if(!colide(j.getX() + j.getSpeed(), j.getY(), j.getTam() ,this.obstaculos))
				j.setX(j.getX() + j.getSpeed());
			break;
			case PConstants.LEFT : if(!colide(j.getX() - j.getSpeed(), j.getY(), j.getTam() ,this.obstaculos))
				j.setX(j.getX() - j.getSpeed());
			break;
			case PConstants.UP   : if(!colide(j.getX(), j.getY() - j.getSpeed(), j.getTam() ,this.obstaculos))
				j.setY(j.getY() - j.getSpeed());
			break;
			case PConstants.DOWN : if(!colide(j.getX(), j.getY() + j.getSpeed(), j.getTam() ,this.obstaculos))
				j.setY(j.getY() + j.getSpeed());
			break;
			default     		 : break;
			}
		}

		if (j.getX() >= this.width-j.getTam()/2) j.setX(this.width-j.getTam()/2);
		if (j.getX() <= j.getTam()/2) j.setX(j.getTam()/2);
		if (j.getY() >= this.height-j.getTam()/2) j.setY(this.height-j.getTam()/2);
		if (j.getY() <= j.getTam()/2) j.setY(j.getTam()/2);

		if(mexeu==true){

		StringBuffer str=new StringBuffer();
		str.append("movePlayer ");
		str.append(j.getName());
		str.append(" ");
		str.append(j.getX());
		str.append(" ");
		str.append(j.getY());
		str.append(" ");
		str.append(j.getTam());
		str.append(" ");
		str.append(j.getAcel());
		str.append(" ");
		str.append(j.getSpeed());
		str.append(" ");
		str.append(j.getPont());

		lig.write(str.toString());
		}
	}

	public void move(Criatura c) {	

	
		int x = c.getX()+(c.getSpeed() * c.getXdir());
		int y = c.getY()+(c.getSpeed() * c.getYdir());

		int xdir = c.getXdir(); 
		int ydir = c.getYdir();

		if (x >= this.width-c.getTam()/2 || x < c.getTam()/2 || colide(x, y, c.getTam() ,this.obstaculos)) {xdir = c.getXdir() * -1;} 
		if (y >= this.height-c.getTam()/2 || y < c.getTam()/2 || colide(x, y, c.getTam() ,this.obstaculos)) {ydir = c.getYdir() * -1;}

		StringBuffer str = new StringBuffer();
		str.append("moveCriat ");
		str.append(c.getNum());	
		str.append(" ");
		str.append(x);
		str.append(" ");
		str.append(y);
		str.append(" ");
		str.append(c.getTam());
		str.append(" ");
		str.append(c.getSpeed());
		str.append(" ");
		str.append(c.getBonus());
		str.append(" ");
		str.append(c.getTipo());
		str.append(" ");
		str.append(xdir);
		str.append(" ");
		str.append(ydir);

		lig.write(str.toString());
	}


	public void setup() {
		size(800,600);
	}

	
	public void draw() {
		background(255);
		for(Corpo c : this.obstaculos) 
			displayObs(c);
		for(Criatura cr : this.criaturas) {
			move(cr);
			displayCriat(cr);
		}
		for(Jogador jgd : this.jogadores.values()) 
			displayJogador(jgd);

		drive(j);
		displayJogador(j);
	}
}
