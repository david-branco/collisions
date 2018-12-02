import java.awt.Color;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

public class Login extends JFrame{
	private JLabel nome,pass,ip,porta;
	private JButton login,register,cancelarConta;
	private JTextField nome1,ip1,porta1;
	private JPasswordField password1;
	private JProgressBar progBar;
	private Ligacao lig = new Ligacao();
	
	
	public Login(){
		super("Arena 2D");
		this.setSize(800,600);
		this.setLocation(250, 50);
		this.setResizable(false);
		this.setBounds(400, 200, 450, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		
		this.nome = new JLabel("Username",JLabel.CENTER);
		this.nome.setBounds(51, 19, 93, 24);
		this.getContentPane().add(this.nome);
		this.pass = new JLabel("Password",JLabel.CENTER);
		this.pass.setBounds(51, 81, 83, 14);
		this.getContentPane().add(this.pass);
		this.ip = new JLabel("IP ",JLabel.CENTER);
		this.ip.setBounds(73, 157, 46, 14);
		this.getContentPane().add(this.ip);
		this.porta = new JLabel("Porta ",JLabel.CENTER);
		this.porta.setBounds(210, 157, 46, 14);
		this.getContentPane().add(this.porta);
		
		this.login = new JButton("Login");
		this.login.setBounds(270, 19, 126, 24);
		this.login.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				make_login(e);
			}
		});
		this.login.setToolTipText("Pressione para continuar para o jogo");
		this.getContentPane().add(this.login);
		
		this.register = new JButton("Registar");
		this.register.setBounds(270, 54, 126, 24);
		this.register.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				make_register(e);
			}
		});
		this.register.setToolTipText("Pressione para se registrar");
		this.getContentPane().add(this.register);
		
		this.cancelarConta = new JButton("Cancelar Conta");
		this.cancelarConta.setBounds(270, 90, 126, 24);
		
		this.cancelarConta.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				 cancel_account(arg0);
			}
		});
		
		this.getContentPane().add(this.cancelarConta);
		this.nome1 = new JTextField();
		this.nome1.setText("sa");
		this.nome1.setBounds(51, 49, 93, 20);
		this.nome1.setToolTipText("Nao usar espaços nem caracteres especiais para alem de _.");
		this.nome1.setColumns(10);
		this.getContentPane().add(this.nome1);
		this.ip1 = new JTextField();
		this.ip1.setBounds(51, 182, 111, 20);
		this.ip1.setColumns(10);
		this.ip1.setText("172.19.98.232");
		this.getContentPane().add(this.ip1);		
		this.porta1 = new JTextField();
		this.porta1.setBounds(182, 182, 111, 20);
		this.porta1.setColumns(10);
		this.porta1.setText("2345");
		this.getContentPane().add(this.porta1);		
		this.password1 = new JPasswordField();
		this.password1.setText("sa");
		this.password1.setEchoChar('*');
		this.password1.setBounds(51, 106, 93, 20);
		this.password1.setColumns(10);
		this.password1.setToolTipText("Apenas permitido letras e numeros inteiros");
		this.getContentPane().add(this.password1);
		
		JLabel label = new JLabel(":");
		label.setBounds(172, 185, 21, 14);
		this.getContentPane().add(label);
		this.progBar = new JProgressBar();
		this.progBar.setBounds(51, 213, 242, 24);
		this.progBar.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.progBar.setForeground(new Color(34, 139, 34));
		this.getContentPane().add(this.progBar);
		
		this.getContentPane().setLayout(null);
		this.getContentPane().setBackground(SystemColor.scrollbar);
		
	}
		
	public static void mostraMensagem(String men){JOptionPane.showMessageDialog(null,men,"Mensagem", JOptionPane.INFORMATION_MESSAGE);}
	
	private boolean is_IPCorrect(String str){return str.matches("[0-9]{1,3}[//.][0-9]{1,3}[//.][0-9]{1,3}[//.][0-9]{1,3}");}
	
	private boolean is_PortaCorrect(String str){return str.matches("[0-9]+");}
	
	private boolean is_Name(String str){ return str.matches("[a-zA-Z0-9_]+");}
	
	private boolean is_Password(String str){return str.matches("[a-zA-Z0-9]+");}
	
	private void make_login(ActionEvent e){
		Runnable r = new Runnable(){
			public void run(){		
				String s1 = new String(nome1.getText());
				String s2 = new String(password1.getPassword());
				String s3 = new String(ip1.getText());
				String s4 = new String(porta1.getText());
				
				if(s1.isEmpty() || s2.isEmpty() || s3.isEmpty() || s4.isEmpty()){
					if(s1.isEmpty()) mostraMensagem("Falta preencher o campo UserName para poder continuar para o jogo");
					else if(s2.isEmpty()) mostraMensagem("Falta preencher o campo PassWord para poder continuar para o jogo");
					else if (s3.isEmpty()) mostraMensagem("Falta preencher o campo IP para poder continuar para o jogo");
					else if (s4.isEmpty()) mostraMensagem("Falta preencher o campo Porta para poder continuar para o jogo");
					else mostraMensagem("Falta preencher alguns campos para poder continuar para o jogo");
				}
				else if(is_Name(s1) == false)
					mostraMensagem("O campo Nome nao pode conter espacos ou caracteres especiais com excepcao do _");
				else if(is_Password(s2) == false)
					mostraMensagem("O campo Password nao pode conter espacos nem caracteres especiais"); 
				else if(is_IPCorrect(s3) == false) 
					mostraMensagem("O campo IP esta mal preenchido.Certifique-se que introduziu correctamente os dados");
				else if(is_PortaCorrect(s4) == false)
					mostraMensagem("O campo Porta esta mal preenchido.Insira apenas numeros.");
				else{
					progBar.setIndeterminate(true);
					int porta = (new Integer(porta1.getText()).intValue());					
					//Ligacao ao servidor
					boolean ok = lig.connect(s3,porta);								
					// Se foi possivel ligar...
					System.out.println("Estado - " + ok);
	                if(ok){
	                	
	                	String buf = new String("login " + s1 + " " + s2);	                	
	                	// Envia o pedido
	                	lig.write(buf);	
	                	// Obtem a resposta
	                	String res = lig.read();
	                	                	
	                    StringTokenizer stk = new StringTokenizer(res," ");	                                     
	                    String token = stk.nextToken();
	                    if(token.equals("logged")){
	                    	
	                    	String nome = stk.nextToken();
	                    	int x = (new Integer(stk.nextToken()).intValue());
	                    	int y = (new Integer(stk.nextToken()).intValue());
	                    	int tam = (new Integer(stk.nextToken()).intValue());
	                    	int acel = (new Integer(stk.nextToken()).intValue());
	                    	int speed = (new Integer(stk.nextToken()).intValue());
	                    	int pont = (new Integer(stk.nextToken()).intValue());
	                    	
	                    	Jogador j = new Jogador(nome,x,y,tam,acel,speed,pont);
	                    		                    	
	                    	String res1 = lig.read();
		                    HashMap<String,Jogador> jgdrs = new HashMap<String,Jogador>();
		                    stk = new StringTokenizer(res1," ");
		                    while(stk.hasMoreTokens()){
		                    	String nomej = stk.nextToken();
		                    	int xj = (new Integer(stk.nextToken()).intValue());
		                    	int yj = (new Integer(stk.nextToken()).intValue());
		                    	int tamj = (new Integer(stk.nextToken()).intValue());
		                    	int acelj = (new Integer(stk.nextToken()).intValue());
		                    	int speedj = (new Integer(stk.nextToken()).intValue());
		                    	int pontj = (new Integer(stk.nextToken()).intValue());
		                    	
		                    	if(!nomej.equals(nome)){
		                    	Jogador jj = new Jogador(nomej,xj,yj,tamj,acelj,speedj,pontj);
		                    	jgdrs.put(nomej, jj);
		                    	}
		                    	
		                    }
	                    	
	                    	String res2 = lig.read();
		                    ArrayList<Corpo> obs = new ArrayList<Corpo>();
		                    stk = new StringTokenizer(res2," ");
		                    while(stk.hasMoreTokens()){
		                    	int no = (new Integer(stk.nextToken()).intValue());
		                    	int xo = (new Integer(stk.nextToken()).intValue());
		                    	int yo = (new Integer(stk.nextToken()).intValue());
		                    	int tamo = (new Integer(stk.nextToken()).intValue());
		                    	Corpo c = new Corpo(xo,yo,tamo);
		                    	obs.add(no,c);
		                    }
	                    	

	                    	String res3 = lig.read();
		                    ArrayList<Criatura> criat = new ArrayList<Criatura>();
		                    stk = new StringTokenizer(res3," ");
		                    while(stk.hasMoreTokens()){
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
		                    	criat.add(nc,cr);
		                    }
		                    
	                    	progBar.setIndeterminate(false);
	                    	DisplayArena arena = new DisplayArena(lig,j,obs,criat,jgdrs);
	                    	arena.setVisible(true);
	                    	dispose();
	                    }
	                    else{
	                    	progBar.setIndeterminate(false);
	                    	if(token.equals("already_in"))
	                    		mostraMensagem("Jogador " + s1 + " ja fez login.");
	                    	else if(token.equals("user_not_found"))
	                    		mostraMensagem("Jogador " + s1 + " nao existe ou com password errada");
	                    }
	                    
	                    
	                }
	                else{
	                	// Nao foi possivel efectuar a ligacao com o servidor.
	                	progBar.setIndeterminate(false);
	                	mostraMensagem("Erro na ligacao ao servidor");
	                	lig.disconnect();
	                }
				}
			}
		};	
		(new Thread(r)).start();	
	}
	
	
	private void make_register(ActionEvent e){
		Runnable r = new Runnable(){
			public void run(){
				String s1 = new String(nome1.getText());
				String s2 = new String(password1.getPassword());
				String s3 = new String(ip1.getText());
				String s4 = new String(porta1.getText());
				if(s1.length() == 0 || s2.length() == 0 || s3.length() == 0 || s4.length() == 0){
					if(s1.length() == 0) mostraMensagem("Falta preencher o campo UserName para poder continuar para o jogo");
					else if(s2.length() == 0) mostraMensagem("Falta preencher o campo PassWord para poder continuar para o jogo");
					else if (s3.length() == 0) mostraMensagem("Falta preencher o campo IP para poder continuar para o jogo");
					else if (s4.length() == 0) mostraMensagem("Falta preencher o campo Porta para poder continuar para o jogo");
					else mostraMensagem("Falta preencher alguns campos para poder continuar para o jogo");
				}
				else if(is_Name(s1) == false)
					mostraMensagem("O campo Nome nao pode conter espacos ou caracteres especiais com excepcao do _");
				else if(is_Password(s2) == false)
					mostraMensagem("O campo Password nao pode conter espacos nem caracteres especiais"); 
				else if(is_IPCorrect(s3) == false) 
					mostraMensagem("O campo IP esta mal preenchido.Certifique-se que introduziu correctamente os dados");
				else if(is_PortaCorrect(s4) == false)
					mostraMensagem("O campo Porta esta mal preenchido.Insira apenas numeros.");
				else{
					progBar.setIndeterminate(true);
					int porta = (new Integer(porta1.getText()).intValue());					
					//Ligacao ao servidor
					boolean ok = lig.connect(s3,porta);								
					// Se foi possivel ligar...
					System.out.println("Estado - " + ok);
	                if(ok){
	                	String buf = new String("regPlayer " + s1 + " " + s2);	                	
	                	// Envia o pedido
	                	lig.write(buf);	
	                	// Obtem a resposta
	                	String res = lig.read();
	                	
	                    StringTokenizer stk = new StringTokenizer(res," ");
	                    String token = stk.nextToken();
	                    progBar.setIndeterminate(false);
	                    if(token.equals("accountcreated"))
	                    	mostraMensagem("Registo efectuado com sucesso.");
	                    else if(token.equals("alreadyexists"))
	                    	mostraMensagem("Utilizador " + s1 + " ja existe. Por favor, escolha outro username.");	                    	
	                }
	                else{
	                	// Nao foi possivel efectuar a ligacao com o servidor.
	                	progBar.setIndeterminate(false);
	                	mostraMensagem("Erro na ligacao ao servidor");
	                	lig.disconnect();
	                }
	              }
			}
		};
		(new Thread(r)).start();
	}

	
	private void cancel_account(ActionEvent e){
		Runnable r = new Runnable(){
			@Override
			public void run() {
				String s1 = new String(nome1.getText());
				String s2 = new String(password1.getPassword());
				String s3 = new String(ip1.getText());
				String s4 = new String(porta1.getText());
				if(s1.length() == 0 || s2.length() == 0 || s3.length() == 0 || s4.length() == 0){
					if(s1.length() == 0) mostraMensagem("Falta preencher o campo UserName para poder cancelar a conta");
					else if(s2.length() == 0) mostraMensagem("Falta preencher o campo PassWord para poder cancelar a conta");
					else if (s3.length() == 0) mostraMensagem("Falta preencher o campo IP para poder cancelar a conta");
					else if (s4.length() == 0) mostraMensagem("Falta preencher o campo Porta para poder cancelar a conta");
					else mostraMensagem("Falta preencher alguns campos para poder cancelar a conta");
				}
				else if(is_Name(s1) == false)
					mostraMensagem("O campo Nome nao pode conter espacos ou caracteres especiais com excepcao do _");
				else if(is_Password(s2) == false)
					mostraMensagem("O campo Password nao pode conter espacos nem caracteres especiais"); 
				else if(is_IPCorrect(s3) == false) 
					mostraMensagem("O campo IP esta mal preenchido.Certifique-se que introduziu correctamente os dados");
				else if(is_PortaCorrect(s4) == false)
					mostraMensagem("O campo Porta esta mal preenchido.Insira apenas numeros.");
				else{
					progBar.setIndeterminate(true);
					int porta = (new Integer(porta1.getText()).intValue());					
					//Ligacao ao servidor
					boolean ok = lig.connect(s3,porta);								
					// Se foi possivel ligar...
					System.out.println("Estado - " + ok);
	                if(ok){
	                	String buf = new String("delPlayer " + s1);	                	
	                	// Envia o pedido
	                	lig.write(buf);	
	                	// Obtem a resposta
	                	String res = lig.read();
	                	//lig.disconnect();
	                    StringTokenizer stk = new StringTokenizer(res," ");
	                    String token = stk.nextToken();
	                    progBar.setIndeterminate(false);
	                    if(token.equals("deleted"))
	                    	mostraMensagem("Utilizador apagado com sucesso.");
	                    else if(token.equals("notdeleted"))
	                    	mostraMensagem("Utilizador " + s1 + " nao existe.Insira os dados correctamente.");
	                    	
	                }
	                else{
	                	// Nao foi possivel efectuar a ligacao com o servidor.
	                	progBar.setIndeterminate(false);
	                	mostraMensagem("Erro na ligacao ao servidor");
	                	lig.disconnect();
	                }
	             }				
			}
			
		};
		(new Thread(r)).start();
	}
}
