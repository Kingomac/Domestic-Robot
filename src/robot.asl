/* Initial beliefs and rules */

// my owner should not consume more than 10 beers a day :-)
limit(beer,5).
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

/** El robot pregunta los precios a los supermercados que le comunicaron su precio al inicio **/
+!update_prices : true <- for (offer(Prod, Prec,Stock)[source(S)]) {
		-offer(Prod, _, _)[source(S)];
		.send(S, askOne, offer(Prod, Y, X));
	}; .print("Precios actualizados").

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
	!go_to(robot, dishwasher);
	get(dish, dishwasher);
	!go_to(robot, cupboard);
	put(dish, cupboard);
	.wait(plate(dishwasher,_));
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
	if(X >= 6) { dishwasher(on) };
	!give(Owner,beer).

+!give(Owner,beer)
   :  not too_much(Owner,beer)
   <- !go_to(robot,fridge);
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
+delivered(Prod, Cant, OrderId)[source(Super)] : not delivered(_,_,_) & Cant < 3 
	& offer(Prod, Precio, _)[source(Super)] <-
	.concat("Pedido de ", Prod,  " recibido con éxito: id ", OrderId, ", ", Cant, " unidades e importe ", Precio, " robux", Mensaje);
  	.send(Super, tell, msg(Mensaje));
	.send(Super, tell, payment(Prod, Cant, Precio)). 
+delivered(Prod, Cant, OrderId)[source(Super)] : delivered(_,Existente,_) & Cant + Existente < 3
	& offer(Prod, Precio, _)[source(Super)] <-
	.concat("Pedido de ", Prod,  " recibido con éxito: id ", OrderId, ", ", Cant, " unidades e importe ", Precio, " robux", Mensaje);
  	.send(Super, tell, msg(Mensaje));
	.send(Super, tell, payment(Prod, Cant, Precio)).
+delivered(Prod,Qtd,OrderId): min_price(Prod,Super) & offer(Prod, P, _)[source(Super)] & money(M) & M > P
  <- get(delivery);
  	.concat("Pedido de ", Prod,  " recibido con éxito: id ", OrderId, ", ", Qtd, " unidades e importe ", P, " robux", Mensaje);
  	.send(Super, tell, msg(Mensaje));
	.send(Super, tell, payment(Prod, Qtd, P)); 
	-+money(M - P);
  	!go_to(storekeeper, fridge);
  	open(fridge);
	save(beer);
	close(fridge);
	!go_to(storekeeper, base_storekeeper).


	
+!add_min_price(Prod, Super) : offer(Prod, _, Cant)[source(Super)] & Cant <= 0  <- true.

+!add_min_price(Prod, Super) : not min_price(Prod, _) <- +min_price(Prod, Super).

+!add_min_price(Prod, Super) : min_price(Prod, OldSuper) &
	offer(Prod, Price, _)[source(Super)] &
	offer(Prod, OldPrice, _)[source(OldSuper)] & OldPrice <= Price <- true.
	
+!add_min_price(Prod, Super) : min_price(Prod, OldSuper) & 
	offer(Prod, Price, _)[source(Super)] &
	offer(Prod, OldPrice, _)[source(OldSuper)] &
	OldPrice > Price <- -min_price(Prod, _); +min_price(Prod, Super).
	
-!add_min_price(Prod, Super) <- .print("Error para add_min_price(", Prod, ",", Super , ")").
	
+!calculate_min_prices <- for(offer(Prod, Price, Stock)[source(Super)]) {
	!add_min_price(Prod, Super);
}; .print("Precios mínimos actualizados").

/** Si hay menos de 2 cervezas se hace un pedido **/
+available(fridge, beer, X) : at(storekeeper, base_storekeeper) & X <= 2 <-
	.print("Stock bajo, se hace pedido");
	!go_to(storekeeper, delivery);
	!update_prices; .wait(1000); !calculate_min_prices; ?favorite(beer, Prod);
	.wait(min_price(Prod,_));
	?min_price(Prod, S); ?offer(Prod, P, Cantidad)[source(Super)]; ?money(DineroActual);
	if(DineroActual <=  P) { !request_money };
	if(Cantidad >= 3) {
	 .send(Super, achieve, order(Prod,3));
	} else {
		.send(Super, achieve, order(Prod,Cantidad));
		!update_prices;.wait(1000);!calculate_min_prices; .wait(1000);
		.send(Super, achieve, order(Prod,3 - Cantidad));
	}.
	
/** Gestión si el supermercado tiene menos de 3 cervezas **/
//+stock(Prod, Num)[source(S)] <- .send(S, achieve, order(Prod, Num)).

/** Código cleaner. Si no está ocupado vacía la papelera o recoge basura si hay **/
+bin(full) <- .wait(free(cleaner)); -free(cleaner); !take_out_trash; +free(cleaner).
+where(trash, A, B) <- .wait(free(cleaner)); -free(cleaner); !clean_trash; +free(cleaner).
+free(cleaner) <- !go_to(cleaner, cleaner_base).
-free(cleaner) <- .drop_intention(go_to(cleaner,cleaner_base)).

+!take_out_trash <- !go_to(cleaner, bin); empty(bin); !go_to(cleaner, delivery); drop(bin). 

+!clean_trash : not where(trash, _, _) <- true.
+!clean_trash : not bin(full) <- !go_to(cleaner, trash); take(trash); !go_to(cleaner, bin); drop(trash).
-!clean_trash <- true.//.wait(1500); !clean_trash.

/** Movimiento simple en diagonal **/
+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe"); .wait(where(Sitio, _, _)); !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) & where(Sitio, DestX,DestY) & at(Tipo, OrigenX, OrigenY) <- puta.nextDirection(OrigenX,OrigenY,DestX,DestY,Direccion); move_robot(Tipo, Direccion); !go_to(Tipo, Sitio).
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio); .wait(3000); !go_to(Tipo, Sitio).
   
+?time(T) : true
  <-  time.check(T).
  

