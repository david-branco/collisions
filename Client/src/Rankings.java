import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Rankings extends JFrame{
		private TreeSet<Jogador> pontuacoes;

		
	public Rankings(Jogador j,HashMap<String,Jogador> jgdrs){
		super("Arena 2D");
		this.setSize(800,600);
		this.setLocation(250, 50);
		this.setResizable(false);
		this.setBounds(400, 200, 450, 300);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.pontuacoes = new TreeSet<Jogador>(new TreePontComparator());
		
		this.pontuacoes.add(j);
		
		for(Jogador jg : jgdrs.values())
			this.pontuacoes.add(jg);
			
		
		this.getContentPane().setLayout(null);
		
		JLabel lblPontos = new JLabel("Pontos");
		lblPontos.setBounds(98, 24, 82, 22);
		lblPontos.setHorizontalAlignment(SwingConstants.CENTER);
		this.getContentPane().add(lblPontos);
		
		JLabel lblJogador = new JLabel("Jogador");
		lblJogador.setBounds(190, 24, 82, 22);
		lblJogador.setHorizontalAlignment(SwingConstants.CENTER);
		this.getContentPane().add(lblJogador);
	
        
		int yP = 45;		
		
		for(Jogador jj : this.pontuacoes){
			criaLabelPontos( ((Integer)jj.getPont()).toString() , 98 , yP ); 
			criaLabelPontos( jj.getName(), 190, yP);
			yP += 21;
			
		}	
		
		}
		private void criaLabelPontos(String pont, int x , int y){
			System.out.println("LABELS");
			JLabel i = new JLabel(pont);
			i.setHorizontalAlignment(SwingConstants.CENTER);
			i.setBounds(x, y, 82, 22);
			this.getContentPane().add(i);
		}
}
