import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import processing.core.PApplet;

public class DisplayArena extends JFrame {

	private JMenuBar menu;
	private JMenu jogador;
	private JMenuItem logout, rankings;
	private Ligacao lig;
	private Jogador j;
	private HashMap<String, Jogador> jogadores;

	public DisplayArena(Ligacao lig, Jogador j, ArrayList<Corpo> obs,
			ArrayList<Criatura> criat, HashMap<String, Jogador> jgdrs) {
		this.lig = lig;
		this.j = j;

		this.jogadores = new HashMap<String, Jogador>();
		for (Jogador jg : jgdrs.values())
			this.jogadores.put(jg.getName(), jg);

		this.menu = new JMenuBar();
		this.setJMenuBar(this.menu);
		/** JMenu Jogador **/
		this.jogador = new JMenu("Jogador");
		this.menu.add(this.jogador);

		this.logout = new JMenuItem("Logout");
		this.logout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				make_logout(arg0);
			}
		});
		this.jogador.add(this.logout);

		/** JMenuItem Rankings **/
		this.rankings = new JMenuItem("Rankings");
		this.rankings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ranking(arg0);
			}
		});
		this.jogador.add(this.rankings);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		PApplet sketch = new Arena(lig, this.j, obs, criat, this.jogadores);
		sketch.init();// this is the function used to start the execution of the sketch						
		panel.add(sketch);
		getContentPane().add(panel);
		this.setSize(800, 650);
		this.setVisible(true);
	}

	private void ranking(ActionEvent e) {
		Runnable r = new Runnable(){
			 public void run(){
				 
				 String buf = new String("pontuacao");
				 lig.write(buf);
				
				 String res = lig.read();
                 
				 jogadores = new HashMap<String,Jogador>();
                 StringTokenizer stk = new StringTokenizer(res," ");
                 String token = stk.nextToken();
                 if(token.equals("pontuacao")){
                	 while(stk.hasMoreTokens()){
                		 String nome = stk.nextToken();
                		 int x = (new Integer(stk.nextToken()).intValue());
                		 int y = (new Integer(stk.nextToken()).intValue());
                		 int tam = (new Integer(stk.nextToken()).intValue());
                		 int acel = (new Integer(stk.nextToken()).intValue());
                		 int speed = (new Integer(stk.nextToken()).intValue());
                		 int pont = (new Integer(stk.nextToken()).intValue());
                 	
                		 Jogador jj = new Jogador(nome,x,y,tam,acel,speed,pont);
                		 jogadores.put(nome, jj);
                	 }
                 }
				 Rankings janela = new Rankings(j,jogadores);
				 janela.setVisible(true);
                 }
		};
		(new Thread(r)).start();
	}

	private void make_logout(ActionEvent e) {
		Runnable r = new Runnable() {
			public void run() {
				String buf = new String("logout " + j.getName());
				lig.write(buf);
				lig.disconnect();
				dispose();
				new Login().setVisible(true);
			}
		};
		(new Thread(r)).start();
	}
}
