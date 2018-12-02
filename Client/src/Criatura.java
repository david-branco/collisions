public class Criatura extends Corpo{

	private int num;
	private int speed;
	private int bonus;
	private int tipo; //1 - comida	2 - venenosa
	private int xdir;
	private int ydir;

	Criatura(int num,int x, int y, int tam, int speed, int bonus, int tipo, int xdir, int ydir) {
		super(x,y,tam);
		this.num = num;
		this.speed = speed;
		this.bonus = bonus;
		this.tipo = tipo;
		this.xdir = xdir;
		this.ydir = ydir;
	}

	public void setSpeed(int speed) { this.speed = speed; }
	public void setXdir(int xdir) { this.xdir = xdir; }
	public void setYdir(int ydir) { this.ydir = ydir;}
	
	public int getXdir() {return this.xdir;}
	public int getYdir() {return this.ydir;}
	public int getSpeed() { return this.speed; }
	public int getBonus() { return this.bonus; }
	public int getTipo() { return this.tipo; }
	public int getNum() {return this.num; }
	
}