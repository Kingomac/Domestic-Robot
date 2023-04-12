/* Initial beliefs and rules */

// my owner should not consume more than 10 beers a day :-)
limit(beer,5).
min_price(beer, 100000000, none). // precio mínimo inicial
free(cleaner). // el cleaner no está ocupado

money(0). // el robot no tiene dinero

too_much(Owner, B) :-
   .date(YY,MM,DD) &
   .count(consumed(Owner,YY,MM,DD,_,_,_,B),QtdB) &
   limit(B,Limit) &
   QtdB > Limit.


/* Plans */

/** Le pide dinero a los owners **/
!request_money.
+!request_money <- .send(owner, achieve, ask_money(robot)).//; .wait({ +money(_) }); .send(owner_musk, achieve, ask_money(robot)). 
+!save_money(Nuevo) <- ?money(Actual); .print("Dinero actual: ", Actual, " que sumado da ", Actual + Nuevo); -+money(Actual + Nuevo).

/** Cuando un supermercado le comunique el precio de la cerveza que actualice el mínimo **/
+price(_,_) : true <- for (price(beer, P)[source(S)]) {
	?min_price(beer, MIN, _);
	if (P < MIN){
		-+min_price(beer, P, S);
	}
}.

/** El robot pregunta los precios a los supermercados que le comunicaron su precio al inicio **/
+!update_prices : true <- for (price(beer, P)[source(S)]) {
		-price(beer, _)[source(S)];
		.send(S, askOne, price(beer, X));
	}.

/** Si el robot tiene la intención de llevarle la cerveza a un owner espera a 
terminar para llevarle cerveza a otro **/ 
+!bring(Owner, beer) : .intend(give(A,_)) & A \== Owner <- .wait(3000); !bring(Owner, beer).
+!bring(Owner, beer) : true <- !give(Owner, beer).
-!bring(_,_)
   :  true
   <- .current_intention(I);
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).

+!save_plates : plate(dishwasher, X) & X == 0 <- true.
+!save_plates : plate(dishwasher, X) & X > 0 <-
	.print("VOY AL LAVAVAJILLAS");
	!go_to(robot, dishwasher);
	.print("SACO PLATO");
	get(dish, dishwasher);
	.print("VOY A LA ALACENA");
	!go_to(robot, cupboard);
	.print("METO PLATO EN LA ALACENA");
	put(dish, cupboard);
	.print("VOY A POR EL SIGUIENTE PLATO");
	.wait(100);
	!save_plates.

+dishwasher(finish) <- !save_plates.
	
//+!give(Owner, beer) : dishwasher(finish) <- !save_plates; !give(Owner, beer).

/** Acción de coger y llevarle al owner correspondiente la cerveza **/
+!give(Owner, beer) : plate(dirty)[source(Owner)] <- 
	take(plate,Owner);
	-plate(dirty)[source(Owner)];
	!go_to(robot, dishwasher);
	put(dish,dishwasher);
	?plate(dishwasher, X);
	if(X >= 5) { dishwasher(on) };
	!give(Owner,beer).

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
	  
/** Si no hay cerveza en la nevera se queda esperando a que haya **/
+!get_when_available(beer) : available(fridge, beer) <- .wait(100); open(fridge); get(beer).
+!get_when_available(beer) : not available(fridge, beer) <- .wait({ +available(fridge,beer) }); !get_when_available(beer). 

/** Cuando llegue el pedido el storekeeper recoge el pedido y hace el pago **/
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

/** Si hay menos de 2 cervezas se hace un pedido **/
+available(fridge, beer, X) : at(storekeeper, base_storekeeper) & X <= 2 <-
	!go_to(storekeeper, delivery);
	!update_prices; .wait(5000); ?min_price(beer, P, S);
	?money(DineroActual); if(DineroActual <=  P) { !request_money };
	.send(S, achieve, order(beer,3)).

/** Código cleaner. Si no está ocupado vacía la papelera o recoge basura si hay **/
+bin(full) <- .wait(free(cleaner)); -free(cleaner); !take_out_trash; +free(cleaner).
+where(trash, A, B) <- .wait(free(cleaner)); -free(cleaner); !clean_trash; +free(cleaner).

+!take_out_trash <- !go_to(cleaner, bin); empty(bin); !go_to(cleaner, delivery); drop(bin); !go_to(cleaner, base_cleaner). 

+!clean_trash : not where(trash, _, _) <- true.
+!clean_trash : not bin(full) <- !go_to(cleaner, trash); take(trash); !go_to(cleaner, bin); drop(trash).
-!clean_trash <- .wait(3000); !clean_trash.

/** Movimiento simple en diagonal **/
+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe"); .wait(where(Sitio, _, _)); !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) & where(Sitio, DestX,DestY) <- move_robot(Tipo, DestX, DestY); !go_to(Tipo, Sitio).
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio); .wait(3000); !go_to(Tipo, Sitio).
   
+?time(T) : true
  <-  time.check(T).
  

