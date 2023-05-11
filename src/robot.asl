/* Initial beliefs and rules */

// my owner should not consume more than 10 beers a day :-)
limit(beer,5).

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
!myrobot.
+!myrobot: plate(dirty)[source(Owner)] <- 
	!go_to(robot,Owner);
	-plate(dirty)[source(Owner)];
	//.wait({ -plate(dirty) });
	take(plate,Owner);
	!go_to(robot, dishwasher);
	put(dish,dishwasher);
	.wait(plate(dishwasher,_));
	?plate(dishwasher, X);
	if(X >= 6) { dishwasher(on) };
	.wait(500);
	!myrobot.
+!myrobot: bring(beer)[source(Owner)] <- !get_dish_for_pincho; !give(Owner,beer); !myrobot.
+!myrobot: dishwasher(finish) <- !save_plates; !myrobot.
+!myrobot: dishwasher(on) <- !go_to(robot, base_robot); .wait(500); !myrobot.
+!myrobot: true <- !go_to(robot, base_robot); .wait(500); !myrobot.

+!save_plates : plate(dishwasher, X) & X == 0 <- true.
+!save_plates : plate(dishwasher, X) & X > 0 <-
	!go_to(robot, dishwasher);
	get(dish, dishwasher);
	!go_to(robot, cupboard);
	put(dish, cupboard);
	.wait(plate(dishwasher,_));
	!save_plates.

//+dishwasher(finish) <- !save_plates.
+!get_dish_for_pincho <- !go_to(robot, cupboard); get(dish, cupboard).
	
//+!give(Owner, beer) : dishwasher(finish) <- !save_plates; !give(Owner, beer).

+!get_when_available(pincho): available(fridge, pincho, N) & N > 0 <- true.
+!get_when_available(pincho): available(fridge, pincho, NP) 
	& NP <= 0 & available(fridge, tapa, NT) & NT > 0 <-  make(pinchos); .wait(2000).
+!get_when_available(pincho): available(fridge, pincho, NP) 
	& NP <= 0 & available(fridge, tapa, NT) & NT <= 0 <-  .wait(200); !get_when_available.

/** Acción de coger y llevarle al owner correspondiente la cerveza **/
+!give(Owner,beer)
   :  not too_much(Owner,beer)
   <- !go_to(robot,fridge);
      !get_when_available(beer);
	  !get_when_available(pincho);
      close(fridge);
      !go_to(robot,Owner);
      hand_in(Owner,beer);
	  hand_in(Owner,pincho);
      ?has(Owner,beer);
      // remember that another beer has been consumed
      .date(YY,MM,DD); .time(HH,NN,SS);
      +consumed(Owner,YY,MM,DD,HH,NN,SS,beer).
	  
+!give(Owner,beer)
   :  too_much(Owner,beer) & limit(beer,L)
   <- .concat("The Department of Health does not allow me to give ", Owner, " more than ", L,
              " beers a day! I am very sorry about that!",M);
      .send(Owner,tell,msg(M));
	  -bring(beer)[source(Owner)].
	  
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

/** Burner **/
!take_out_trash.
+!take_out_trash: bin(full) <- !go_to(burner, bin); empty(bin); .wait(5000); drop(bin); !take_out_trash.
+!take_out_trash: true <- !go_to(burner, base_burner); .wait({ +bin(full) }); !take_out_trash.

/** Cleaner. Recoge basura si hay **/
!clean_trash.
+!clean_trash: where(trash, _, _) <- !go_to(cleaner, trash); take(trash); !go_to(cleaner, bin); drop(trash); !clean_trash. 
+!clean_trash: true <- !go_to(cleaner, base_cleaner); .wait(where(trash,_,_)); !clean_trash.

/** Movimiento **/
+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe"); .wait(where(Sitio, _, _)); !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) & where(Sitio, DestX,DestY) & at(Tipo, OrigenX, OrigenY) <- movement.NextDirection(OrigenX,OrigenY,DestX,DestY,Tipo,AA); move_agent(Tipo, AA); !go_to(Tipo, Sitio).
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio); .wait(3000); !go_to(Tipo, Sitio).
   
+?time(T) : true
  <-  time.check(T).
  

