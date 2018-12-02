public class Jogador extends Corpo{
	
	  private String name; 
	  private int speed;
	  private int acel;
	  private int pont;
	  
	  
	  Jogador(String name, int x, int y, int tam, int acel, int speed, int pont) {
		super(x,y,tam);  
	    this.name = name;
	    this.acel = acel;
	    this.speed = speed;
	    this.pont = pont;
	  }
	  
	 
	  public String getName() { return this.name; }
	  public int getSpeed() { return this.speed; }
	  public int getPont() { return this.pont; }
	  public int getAcel() { return this.acel; }
	  
	  public void setAcel(int acel) { this.acel = acel; }
	  public void setSpeed(int speed) { this.speed = speed; }
	  public void setPont(int pont) { this.pont = pont; }
	  	  
}