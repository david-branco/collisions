public class Corpo {

	private int x;
	private int y;  
	private int tam;
	private final int maxTam = 40;

	public Corpo(int x,int y,int tam){
		this.tam = tam;
		this.x = x;
		this.y = y;    
	}

	public int getX(){return this.x;}
	public int getY(){return this.y;} 
	public int getTam() {return this.tam; }
	public int getMaxTam() {return this.maxTam; }

	public void setX(int x){this.x = x;}
	public void setY(int y){this.y = y;}
	public void setTam(int tam){this.tam = tam;}
}