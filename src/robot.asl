/* Initial beliefs and rules */

// my owner should not consume more than 10 beers a day :-)
limit(beer,5).
min_price(beer, 100000000, none).
free(cleaner).

money(0). 

too_much(Owner, B) :-
   .date(YY,MM,DD) &
   .count(consumed(Owner,YY,MM,DD,_,_,_,B),QtdB) &
   limit(B,Limit) &
   QtdB > Limit.


/* Plans */
!request_money.
+!request_money <- .send(owner, achieve, ask_money(robot)); .wait({ +money(_) }); .send(owner_musk, achieve, ask_money(robot)). 
+!save_money(Nuevo) <- ?money(Actual); .print("Dinero actual: ", Actual, " que sumado da ", Actual + Nuevo); -+money(Actual + Nuevo).

+price(_,_) : true <- for (price(beer, P)[source(S)]) {
	?min_price(beer, MIN, _);
	if (P < MIN){
		-+min_price(beer, P, S);
	}
}.

+!update_prices : true <- for (price(beer, P)[source(S)]) {
		-price(beer, _)[source(S)];
		.send(S, askOne, price(beer, X));
	}.

+!bring(Owner, beer) : .intend(give(A,_)) & A \== Owner <- .wait(3000); !bring(Owner, beer).
+!bring(Owner, beer) : true <- !give(Owner, beer).

+!give(Owner,beer)
   :  not too_much(Owner,beer)
   <- !go_to(robot,fridge);
      open(fridge);
      !get_when_available(beer);
      close(fridge);
      !go_to(robot,Owner);
      hand_in(Owner,beer);
      ?has(Owner,beer);
      // remember that another beer has been consumed
      .date(YY,MM,DD); .time(HH,NN,SS);
      +consumed(Owner,YY,MM,DD,HH,NN,SS,beer).
	  
+!give(Owner,beer)
   :  too_much(Owner,beer) & limit(beer,L)
   <- .concat("The Department of Health does not allow me to give ", Owner, " more than ", L,
              " beers a day! I am very sorry about that!",M);
      .send(Owner,tell,msg(M)).
	  
-!give(_,_)
   :  true
   <- .current_intention(I);
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).
/*
+!bring(Owner,beer)
   :  not too_much(Owner,beer)
   <- !go_to(robot,fridge);
      open(fridge);
      !get_when_available(beer);
      close(fridge);
      !go_to(robot,Owner);
      hand_in(Owner,beer);
      ?has(Owner,beer);
      // remember that another beer has been consumed
      .date(YY,MM,DD); .time(HH,NN,SS);
      +consumed(Owner,YY,MM,DD,HH,NN,SS,beer).
	  
+!bring(Owner,beer)
   :  too_much(Owner,beer) & limit(beer,L)
   <- .concat("The Department of Health does not allow me to give ", Owner, " more than ", L,
              " beers a day! I am very sorry about that!",M);
      .send(Owner,tell,msg(M)).
*/

+!get_when_available(beer) : available(fridge, beer) <- .wait(100); open(fridge); get(beer).
+!get_when_available(beer) : not available(fridge, beer) <- .wait({ +available(fridge,beer) }); !get_when_available(beer). 

/*+!bring(owner,beer)
   :  not available(beer,fridge)
   <- .print("Se acabaron las cervezas"); .wait({+available(beer, frige)}); !bring(owner,beer) . // go to fridge and wait there.*/

-!bring(_,_)
   :  true
   <- .current_intention(I);
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).

+delivered(beer,Qtd,OrderId): min_price(beer, P, S) & money(M) & M > P
  <- get(delivery);
  	.concat("Pedido recibido con éxito: beer(", OrderId, "), ", Qtd, " unidades e importe ", P, " robux", Mensaje);
  	.send(S, tell, msg(Mensaje));
	.send(S, tell, payment(beer, Qtd, P)); 
	-+money(M - P);
  	!go_to(storekeeper, fridge);
  	open(fridge);
	save(beer);
	close(fridge);
	!go_to(storekeeper, base_storekeeper).

+available(fridge, beer, X) : at(storekeeper, base_storekeeper) & X <= 2 <-
	!go_to(storekeeper, delivery);
	!update_prices; .wait(5000); ?min_price(beer, P, S);
	?money(DineroActual); if(DineroActual <=  P) { !request_money };
	.send(S, achieve, order(beer,3)).

+bin(full) <- .wait(free(cleaner)); -free(cleaner); !take_out_trash; +free(cleaner).
+where(trash, A, B) <- .wait(free(cleaner)); -free(cleaner); !clean_trash; +free(cleaner).

+!take_out_trash <- !go_to(cleaner, bin); empty(bin); !go_to(cleaner, delivery); drop(bin); !go_to(cleaner, base_cleaner). 

+!clean_trash : not where(trash, _, _) <- true.
+!clean_trash : not bin(full) <- !go_to(cleaner, trash); take(trash); !go_to(cleaner, bin); drop(trash).
-!clean_trash <- .wait(3000); !clean_trash.


+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe"); .wait(where(Sitio, _, _)); !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) & where(Sitio, DestX,DestY) <- move_robot(Tipo, DestX, DestY); !go_to(Tipo, Sitio).
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio); .wait(3000); !go_to(Tipo, Sitio).
   
+?time(T) : true
  <-  time.check(T).
  

