-module(servidor).
-export([start_servidor/0,clientProcess/1,removeEOL/1,converter/1,cria_maps/0]).

sleep(T) ->
    receive
    after T ->
       true
    end. 

% update(Pid) ->
% 	receive 
% 		after 5000 ->
% 			Pid ! {atualiza}
% 	end.

start_servidor() ->
	spawn(fun() ->
			start_servidor_paralelo(2345),
			sleep(infinity)
              end).			


%%Começar o servidor com uma porta 
start_servidor_paralelo(Porta) ->
	{ok, Listen} = gen_tcp:listen(Porta,[{packet,line},{reuseaddr,true},list]),
		  PidAdmin = spawn(fun() -> loop(cria_maps()) end),
          register(pid_admin,PidAdmin),
          register(mainServer,self()),
	spawn(fun() -> novo_cliente(Listen) end),
	%update(pid_admin),
	io:format("Servidor iniciado!~n").


novo_cliente(Listen) ->
	{ok, Socket} = gen_tcp:accept(Listen),
	Pid = spawn(fun() -> clientProcess(Socket) end),
	gen_tcp:controlling_process(Socket, Pid),
	novo_cliente(Listen),
	io:format("Pedido a processar!~n").

clientProcess(Socket) ->
	receive
		{tcp, Socket, Data} ->
			io:format("Servidor recebeu o seguinte pedido = ~p~n",[Data]),
        	case converter(removeEOL(Data)) of
        		{login,[Username,Password]} ->
                	pid_admin ! {login,Username,Password,self()},
                		receive
                            {ok,loginSucessful}->
                                   	pid_admin ! {newUser,Username,self()},
                                   		receive
                                   	         {userCreated,Resposta,Obs,Criat,Jogadores} ->
                                   	              gen_tcp:send(Socket,Resposta),
                                   	              %io:format("Jogadores : ~p~n",[jogadores_para_string(Jogadores)]),                                	            
                                   	              gen_tcp:send(Socket,jogadores_para_string(Jogadores)++"\n"),
                                   	              gen_tcp:send(Socket,obstaculos_para_string(Obs)++"\n"),
                                   	              gen_tcp:send(Socket,criaturas_para_string(Criat)++"\n")
                                   		end,
                                   	clientProcess(Socket);
                            {error,userNotFound}->
                                 	gen_tcp:send(Socket,"user_not_found\n"),
                                    clientProcess(Socket);
                            {error,userAlreadyLogged}->
                                    gen_tcp:send(Socket,"already_in\n"),
                                    clientProcess(Socket) 		                                        
						end;

				{regPlayer,[Username,Password]}->
                    pid_admin ! {regPlayer,Username,Password,self()},
						receive
							{ok,userIn}->
								gen_tcp:send(Socket,"accountcreated\n"),
								io:format("Conta criada ~n");
							{error,alreadyExists}->
								gen_tcp:send(Socket,"alreadyexists\n"),
								io:format("erro ao criar a conta ~n")            
						end,
						clientProcess(Socket);

				{logout,[Username]}->
                    pid_admin ! {logout,Username,self()},
						receive
							{logout,Msg}->
								gen_tcp:send(Socket,Msg),
								io:format("Utilizador fez logout~n")          
						end,
						clientProcess(Socket);

                {delPlayer,[Username]}->
					pid_admin ! {delPlayer,Username,self()},
						receive
							{ok,accountCanceled}->
								io:format("conta apagada com sucesso ~n"),
								gen_tcp:send(Socket,"deleted\n");
							{error,userNotFound}->
								io:format("erro ao apagar a conta ~n"),
								gen_tcp:send(Socket,"notdeleted\n")            
						end,
						clientProcess(Socket);
				{moveCriat,[N,X,Y,Tam,Speed,Bonus,Tipo,Xdir,Ydir]}->
					pid_admin ! {moveCriat,[N,X,Y,Tam,Speed,Bonus,Tipo,Xdir,Ydir],self()},
						receive
							{criatMoved,Msg}->
								%String = "criatMoved "++criatura_para_string({list_to_integer(N),CriatValue})++"\n",
								gen_tcp:send(Socket,Msg);
							{criatDead} ->
								gen_tcp:send(Socket,"criatDead\n")    
						end,
						clientProcess(Socket);		
				{movePlayer,P}->
					%io:format("jgMoved ~p~n",[string_para_jogador(P)]),
					pid_admin ! {movePlayer,P,self()},
						receive
							{jgMoved,Msg}->
								gen_tcp:send(Socket,Msg);
							{jgUpdated,Msg1,Msg2}->
								gen_tcp:send(Socket,Msg1),
								gen_tcp:send(Socket,Msg2);
							{criDie,MsgJ,MsgC}->
								gen_tcp:send(Socket,MsgJ),
								gen_tcp:send(Socket,MsgC);
							{notjgMoved} ->
								gen_tcp:send(Socket,"jgDead\n")    
						end,
						clientProcess(Socket);
				{pontuacao,[]} ->
					pid_admin ! {pontuacao,self()},
						receive 
							{listaPontuacao, ListaJogs}->
								gen_tcp:send(Socket,ListaJogs)
						end,
						clientProcess(Socket)
			end;			
		{propag,MsgProg} ->
			io:format("Mensagem ~p propagada!!!~n",[MsgProg]),		
			gen_tcp:send(Socket,MsgProg),
			clientProcess(Socket);
        { tcp_closed, Socket } -> 
    		io:format("tcp-closed~p~n",[Socket]),
			pid_admin ! {exit,self()};						
        { tcp_error, Socket } ->
            io:format("tcp-error~n"),
			pid_admin ! {exit,self()}
	end.

	      
loop([OnlinePlayers,PlayersPos,Criaturas,Obstaculos]) -> 
	io:format("OnlinePlayer ~p~n",[OnlinePlayers]),
	io:format("Posicoes dos Players ~p~n",[PlayersPos]),
	io:format("Criaturas ~p~n",[Criaturas]),
	io:format("Obstaculos ~p~n",[Obstaculos]),
	receive
		{movePlayer,Player,From} ->
			{U,{X, Y, Tam, Acel, Speed, Pont}} = string_para_jogador(Player),
			case colide_jogadores({U,{X, Y, Tam, Acel, Speed, Pont}}, maps:without([U],PlayersPos), Criaturas, Obstaculos) of
				false ->
					case colide_jogador_criaturas({U,{X, Y, Tam, Acel, Speed, Pont}},PlayersPos,Criaturas,Obstaculos) of
						false ->
							NewPlayerPos = maps:update(U,{X, Y, Tam, Acel, Speed, Pont},PlayersPos),
							Resposta = "jgMoved "++string:join(Player," ")++"\n",
							From ! {jgMoved,Resposta},
							propagar_msg(From,Resposta,OnlinePlayers),
							loop([OnlinePlayers,NewPlayerPos,Criaturas,Obstaculos]);
						{{U,NJ}, {N,NC}} ->
							UpdateJog = maps:update(U,NJ,PlayersPos),
							UpdateCri = maps:update(N,NC,Criaturas),
							RespostaJog = "jgUpdated "++jogador_para_string({U,NJ})++"\n",
							RespostaCri = "criatMoved "++criatura_para_string({N,NC})++"\n",
							From ! {criDie,RespostaJog,RespostaCri},
							propagar_msg(From,RespostaJog,OnlinePlayers),
							propagar_msg(From,RespostaCri,OnlinePlayers),
							loop([OnlinePlayers,UpdateJog,UpdateCri,Obstaculos])
					end;
				{{U1,N1}, {U2,N2}} ->
					Update1 = maps:update(U1,N1,PlayersPos),
					Update2 = maps:update(U2,N2,Update1),
					Resposta1 = "jgUpdated "++jogador_para_string({U1,N1})++"\n",
					Resposta2 = "jgUpdated "++jogador_para_string({U2,N2})++"\n",
					From ! {jgUpdated,Resposta1,Resposta2},
					propagar_msg(From,Resposta1,OnlinePlayers),
					propagar_msg(From,Resposta2,OnlinePlayers),
					loop([OnlinePlayers,Update2,Criaturas,Obstaculos])
				end;					
		{moveCriat,C,From} ->
			{N,X,Y,Tam,Speed,Bonus,Tipo,Xdir,Ydir} = string_para_criatura(C),
			case colide_criatura({X,Y,Tam}, maps:without([N],Criaturas), Obstaculos) of
				false -> 
					NewCriaturas = maps:update(N,{X,Y,Tam,Speed,Bonus,Tipo, Xdir,Ydir},Criaturas),
					Resposta = "criatMoved "++string:join(C," ")++"\n";
				true ->
					NewCriaturas = maps:update(N,{X,Y,Tam,Speed,Bonus,Tipo,-Xdir,-Ydir},Criaturas),
					Resposta = "criatMoved "++criatura_para_string({N,{X,Y,Tam,Speed,Bonus,Tipo,-Xdir,-Ydir}})++"\n"
				end,
			From ! {criatMoved,Resposta},
			propagar_msg(From,Resposta,OnlinePlayers),
			loop([OnlinePlayers,PlayersPos,NewCriaturas,Obstaculos]);
		{newUser,U,From} ->
				NJogador = criar_jogador(PlayersPos, Criaturas, Obstaculos),
				Resposta = "logged "++jogador_para_string({U,NJogador})++"\n",
				NewPlayersPos = maps:put(U,NJogador,PlayersPos),
				From ! {userCreated,Resposta,maps:to_list(Obstaculos),maps:to_list(Criaturas),maps:to_list(NewPlayersPos)},
				propagar_msg(From,Resposta,OnlinePlayers),
				loop([OnlinePlayers,NewPlayersPos,Criaturas,Obstaculos]);
		{regPlayer,U,P,From} ->
			case maps:find(U,OnlinePlayers) of
				error -> 
					From ! {ok,userIn},
					loop([maps:put(U,{P,false,From},OnlinePlayers),PlayersPos,Criaturas,Obstaculos]);
				_->
					From ! {error,alreadyExists},
					loop([OnlinePlayers,PlayersPos,Criaturas,Obstaculos])
			end;
		{delPlayer,U,From} ->
			case maps:find(U,OnlinePlayers) of
				error -> 
					From ! {error,userNotFound},
					loop([OnlinePlayers,PlayersPos,Criaturas,Obstaculos]);
				_ ->
					From ! {ok,accountCanceled},
					loop([maps:remove(U,OnlinePlayers),PlayersPos,Criaturas,Obstaculos])
			end;
		{login,U,P,From} ->
			case maps:find(U,OnlinePlayers) of
				{ok,{P,true,_}} ->
					From ! {error,userAlreadyLogged},
					loop([OnlinePlayers,PlayersPos,Criaturas,Obstaculos]);
				{ok,{P,false,_}} ->
					From ! {ok,loginSucessful},
					loop([maps:update(U,{P,true,From},OnlinePlayers),PlayersPos,Criaturas,Obstaculos]);
				_ ->
					From ! {error,userNotFound},
					loop([OnlinePlayers,PlayersPos,Criaturas,Obstaculos])	
			end;
		{logout,U,From} ->
			case maps:find(U,OnlinePlayers) of
				{ok,{P,true,From}} ->
					Msg = "logout "++U++"\n",
					From ! {logout, Msg},
					propagar_msg(From,Msg,OnlinePlayers),
					loop([maps:update(U,{P,false,From},OnlinePlayers),maps:remove(U,PlayersPos),Criaturas,Obstaculos])
			end;
		{pontuacao,From} ->
			ListaJogs = maps:to_list(PlayersPos),
			Msg = "pontuacao "++jogadores_para_string(ListaJogs)++"\n",
			From ! {listaPontuacao, Msg},
			loop([OnlinePlayers,PlayersPos,Criaturas,Obstaculos])
		% {atualiza} ->
		% 	ListaJog = [{X,Y,alt_tam(dec,Tam),Acel,alt_vel(inc,Speed),Pont} || {X,Y,Tam,Acel,Speed,Pont} <- maps:values(PlayersPos)],
		% 	loop([OnlinePlayers,ListaJog,Criaturas,Obstaculos])
	end.



cria_maps() ->
		OnlinePlayers = maps:new(),
	 	PlayersPos = maps:new(),
	 	Obstaculos = populateObs(maps:new(),3),
		Criaturas = populateCriat(maps:new(), Obstaculos, 5),		
		[OnlinePlayers,PlayersPos,Criaturas,Obstaculos].	


removeEOL(String)-> string:substr(String,1,(string:len(String)-1)).

converter(String)->
        case string:tokens(String," ") of 
                [H|T]->{list_to_atom(H),T};
                []->error
        end.


propagar_msg(MyPid,Msg,OnlinePlayers) ->

	Pids = [Pid || {_,{_,true,Pid}} <- maps:to_list(OnlinePlayers)],
	propagar_msg_aux(Msg,Pids -- [MyPid]).


propagar_msg_aux(_,[]) -> io:format("Mensagem foi propagada para todos!!! ~n");
propagar_msg_aux(Msg,[H|T]) ->
                io:format("Propagar ~p  para ~p~n",[Msg,H]),	
		H ! {propag,Msg},
		propagar_msg_aux(Msg,T).        



obstaculo_para_string({N, {X,Y,Tam}}) ->
	Lista = [integer_to_list(N), integer_to_list(X), integer_to_list(Y), integer_to_list(Tam)],
	string:join(Lista, " ").

obstaculos_para_string([]) -> "";
obstaculos_para_string([H]) -> obstaculo_para_string(H);
obstaculos_para_string([H|T]) -> obstaculo_para_string(H) ++ " " ++ obstaculos_para_string(T).

criatura_para_string({N, {X, Y, Tam, Speed, Bonus, Tipo, Xdir, Ydir}}) ->
	Lista = [integer_to_list(N), integer_to_list(X), integer_to_list(Y), integer_to_list(Tam), integer_to_list(Speed), integer_to_list(Bonus), integer_to_list(Tipo), integer_to_list(Xdir), integer_to_list(Ydir)],
	string:join(Lista, " ").

criaturas_para_string([]) -> "";
criaturas_para_string([H]) -> criatura_para_string(H);
criaturas_para_string([H|T]) -> criatura_para_string(H) ++ " " ++ criaturas_para_string(T).


jogador_para_string({U, {X, Y, Tam, Acel, Speed, Pont}}) ->
	Lista = [U, integer_to_list(X), integer_to_list(Y), integer_to_list(Tam), integer_to_list(Acel), integer_to_list(Speed), integer_to_list(Pont)],
	string:join(Lista, " ").

jogadores_para_string([]) -> "";
jogadores_para_string([H]) -> jogador_para_string(H);
jogadores_para_string([H|T]) -> jogador_para_string(H) ++ " " ++ jogadores_para_string(T).

string_para_criatura([N, X, Y, Tam, Speed, Bonus, Tipo, Xdir, Ydir]) ->
	{list_to_integer(N), list_to_integer(X), list_to_integer(Y), list_to_integer(Tam), list_to_integer(Speed), list_to_integer(Bonus), list_to_integer(Tipo), list_to_integer(Xdir), list_to_integer(Ydir)}
	.

string_para_jogador([U, X, Y, Tam, Acel, Speed, Pont]) ->
	{U, {list_to_integer(X), list_to_integer(Y), list_to_integer(Tam), list_to_integer(Acel), list_to_integer(Speed), list_to_integer(Pont)}}
	.

colide_criatura({X,Y,Tam}, Criaturas, Obstaculos) ->
	ListaCri = [{X1,Y1,Tam1} || {X1,Y1,Tam1,_,_,_,_,_} <- maps:values(Criaturas) ],
	case compara_tuplos({X,Y,Tam},ListaCri) of
		true -> true;
		false ->
			ListaObs = [{X1,Y1,Tam1*2} || {X1,Y1,Tam1} <- maps:values(Obstaculos) ],
			case compara_tuplos({X,Y,Tam},ListaObs) of
				true -> true;
				false -> false
			end
	end.

colide_jogadores({U,{X,Y,Tam,Acel,Speed,Pont}}, PlayersPos, Criaturas, Obstaculos) ->
	ListaJog= maps:to_list(PlayersPos),
    case ListaJog of
    	[] -> false;
    	_ ->
			case compara_jogadores({X,Y,Tam}, ListaJog) of
				false -> false;
				{U2,{X2,Y2,Tam2,Acel2,Speed2,Pont2}} ->
					case Tam == Tam2 of
						true -> false;
						false ->
							case Tam > Tam2 of
								true -> NJogador = criar_jogador(maps:put(U,{X,Y,Tam,Acel,Speed,Pont},PlayersPos), Criaturas, Obstaculos),
									{{U,{X,Y,alt_tam(aum,Tam),Acel,alt_vel(dec,Speed),Pont+1} },{U2,NJogador}}; 
								false -> N2Jogador = criar_jogador(PlayersPos, Criaturas, Obstaculos), 
									{{U,N2Jogador},{U2,{X2,Y2,alt_tam(aum,Tam2),Acel2,alt_vel(dec,Speed2),Pont2+1}}}
							end
					end
			end
	end. 

colide_jogador_criaturas({U,{X,Y,Tam,Acel,Speed,Pont}}, PlayersPos, Criaturas, Obstaculos) ->
	ListaCri = maps:to_list(Criaturas),
	case compara_jogador_criaturas({X,Y,Tam}, ListaCri) of
		false -> false;
		{N2,Tipo2} ->
			NCriatura = criar_criatura(PlayersPos, Criaturas, Obstaculos),
			case Tipo2 of
				1 -> {{U,{X,Y,alt_tam(aum,Tam),Acel,alt_vel(dec,Speed),Pont}},{N2,NCriatura}};
				2 -> {{U,{X,Y,alt_tam(dim,Tam),Acel,alt_vel(inc,Speed),Pont}},{N2,NCriatura}}
			end
	end.


compara_jogador_criaturas(_,[]) -> false;
compara_jogador_criaturas({X,Y,Tam},[{N1,{X1,Y1,Tam1,_,_,Tipo1,_,_}} | T]) -> 
	case check({X,Y,Tam},{X1,Y1,Tam1}) of
		true -> {N1,Tipo1};
		false -> compara_jogador_criaturas({X,Y,Tam},T)
	end.


compara_jogadores(_,[]) -> false;
compara_jogadores({X,Y,Tam},[{U1,{X1,Y1,Tam1,Acel1,Speed1,Pont1}} | T]) -> 
	case check({X,Y,Tam},{X1,Y1,Tam1}) of
		true -> {U1,{X1,Y1,Tam1,Acel1,Speed1,Pont1}};
		false -> compara_jogadores({X,Y,Tam},T)
	end.

	

criar_jogador(PlayersPos, Criaturas, Obstaculos) ->
	Tam = 15, 
	X = crypto:rand_uniform(Tam,800-Tam),
	Y = crypto:rand_uniform(Tam,600-Tam),
	Acel = 0,
	Speed = 6,
	Pont = 0,
	ListaJog= [{X1,Y1,Tam1} || {X1,Y1,Tam1,_,_,_} <- maps:values(PlayersPos) ],
	case compara_tuplos({X,Y,Tam},ListaJog) of
		true -> criar_jogador(PlayersPos, Criaturas, Obstaculos);
		false -> 
			ListaCri = [{X1,Y1,Tam1} || {X1,Y1,Tam1,_,_,_,_,_}  <- maps:values(Criaturas) ],
			case compara_tuplos({X,Y,Tam},ListaCri) of
				true -> criar_jogador(PlayersPos, Criaturas, Obstaculos);
				false ->
					ListaObs = [{X1,Y1,Tam1*2} || {X1,Y1,Tam1} <- maps:values(Obstaculos) ],
					case compara_tuplos({X,Y,Tam},ListaObs) of
						true -> criar_jogador(PlayersPos, Criaturas, Obstaculos);
						false -> {X,Y,Tam,Acel,Speed,Pont}
					end
			end
	end.

criar_criatura(PlayersPos, Criaturas, Obstaculos) ->
	Tam = crypto:rand_uniform(10,41),
	X = crypto:rand_uniform(Tam,800-Tam),
	Y = crypto:rand_uniform(Tam,600-Tam),
	Speed = round(100/Tam),
	Tipo = random:uniform(2),
	Bonus = getBonus(Tipo,Tam),
	{Xdir,Ydir} = geraDir(),

	ListaJog= [{X1,Y1,Tam1} || {X1,Y1,Tam1,_,_,_} <- maps:values(PlayersPos) ],
	case compara_tuplos({X,Y,Tam},ListaJog) of
		true -> criar_criatura(PlayersPos, Criaturas, Obstaculos);
		false -> 
			ListaCri = [{X1,Y1,Tam1} || {X1,Y1,Tam1,_,_,_,_,_}  <- maps:values(Criaturas) ],
			case compara_tuplos({X,Y,Tam},ListaCri) of
				true -> criar_criatura(PlayersPos, Criaturas, Obstaculos);
				false ->
					ListaObs = [{X1,Y1,Tam1*2} || {X1,Y1,Tam1} <- maps:values(Obstaculos) ],
					case compara_tuplos({X,Y,Tam},ListaObs) of
						true -> criar_criatura(PlayersPos, Criaturas, Obstaculos);
						false -> {X,Y,Tam,Speed,Bonus,Tipo,Xdir,Ydir}
					end
			end
	end.

populateCriat(MCri, _,-1) -> MCri;
populateCriat(MCri, MObs, N) ->
	Tam = crypto:rand_uniform(10,41),
	X = crypto:rand_uniform(Tam,800-Tam),
	Y = crypto:rand_uniform(Tam,600-Tam),
	Speed = round(100/Tam),
	Tipo = random:uniform(2),
	Bonus = getBonus(Tipo,Tam),
	{Xdir,Ydir} = geraDir(),
	ListaCri = [{X1,Y1,Tam1} || {X1,Y1,Tam1,_,_,_,_,_} <- maps:values(MCri) ],
	case compara_tuplos({X,Y,Tam},ListaCri) of
		true -> populateCriat(MCri,MObs,N);
		false -> 
			ListaObs = [{X1,Y1,Tam1*2} || {X1,Y1,Tam1} <- maps:values(MObs) ],
			case compara_tuplos({X,Y,Tam},ListaObs) of
				true -> populateCriat(MCri,MObs,N);
				false -> populateCriat(maps:put(N,{X,Y,Tam,Speed,Bonus,Tipo,Xdir,Ydir},MCri), MObs, N-1)
			end
	end.

alt_vel(Token, Speed) ->
	case Token of 
		inc ->
			case Speed < 6 of 
				true -> Speed+1;
				false -> Speed
			end;
		dec -> 
			case Speed > 1 of 
				true -> Speed-1;
				false -> Speed
			end
	end.	

alt_tam(Token, Tam) ->
	case Token of 
		aum ->
			case Tam < 35 of 
				true -> Tam+5;
				false -> Tam
			end;
		dim -> 
			case Tam > 15 of 
				true -> Tam-5;
				false -> Tam
			end
	end.


geraDir() ->
	Xdir = crypto:rand_uniform(-1,1),
	Ydir = crypto:rand_uniform(-1,1),
	case {Xdir,Ydir} of 
		{0,0} -> geraDir();
		_ -> {Xdir,Ydir}
	end.


getBonus(Tipo,Bonus) -> 
	case Tipo of
			1 -> Bonus;
			2 -> Bonus * -1
		end.


populateObs(M,-1) -> M;
populateObs(M,N) -> 
	Tam = crypto:rand_uniform(10,41),
	X = crypto:rand_uniform(Tam,800-Tam),
	Y = crypto:rand_uniform(Tam,600-Tam),
	case compara_tuplos({X,Y,Tam},maps:values(M)) of
		true -> populateObs(M,N);
		false -> populateObs(maps:put(N,{X,Y,Tam},M),N-1)
	end.


compara_tuplos(_,[]) -> false;
compara_tuplos({X,Y,Tam},[H | T]) -> 
	case check({X,Y,Tam},H) of
		true -> true;
		false -> compara_tuplos({X,Y,Tam},T)
	end.

check({X,Y,Tam},{X1,Y1,Tam1}) ->
	Dist = math:sqrt( math:pow(( X - X1 ),2) + math:pow(( Y - Y1 ),2) ),
	case (Dist =< Tam/2 + Tam1/2) of
		true -> true;
		false -> false
	end.