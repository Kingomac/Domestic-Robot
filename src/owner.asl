money(1000).
favorite(beer, estrella).
favorite(pincho, durum).

/* Initial goals */

!tell_preferences.
!get(beer).   // initial goal: get a beer
!check_bored. // initial goal: verify whether I am getting bored

+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe"); .wait(where(Sitio, _, _)); !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) & where(Sitio, DestX,DestY) & at(Tipo, OrigenX, OrigenY) <- movement.NextDirection(OrigenX,OrigenY,DestX,DestY,Tipo,AA); move_agent(Tipo, AA); !go_to(Tipo, Sitio).
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio); .wait(3000); !go_to(Tipo, Sitio).


+!tell_preferences <- for(favorite(Prod, Pref)) {
	.send(robot, tell, favorite(Prod, Pref));
}.
// Darle dinero al robot si se lo pide
+!ask_money(Ag) : money(X) & X > 0 <- .send(Ag, achieve, save_money(X * 0.5)); -+money(X * 0.5).
+!get(beer): .intend(drink(beer)) <- .wait(100); !get(beer).
+!get(beer): not .intend(get_dish_for_pincho) & not .intend(give(owner,beer)) & .random(Rand) & Rand < 0.25 <-.send(robot,askOne, too_much(owner,beer),A); !get_dish_for_pincho; !give(owner,beer).
+!get(beer) : true
   <- .send(robot, tell, bring(beer)).

/** Puede coger una cerveza y un pincho por si mismo **/ 
+!get_dish_for_pincho <- !go_to(owner, cupboard); get(dish, cupboard).
+!get_when_available(pincho): available(fridge, pincho, N) & N > 0 <- true.
+!get_when_available(pincho): available(fridge, pincho, NP) 
	& NP <= 0 & available(fridge, tapa, NT) & NT > 0 <-  make(pinchos); .wait(2000).
+!get_when_available(pincho): available(fridge, pincho, NP) 
	& NP <= 0 & available(fridge, tapa, NT) & NT <= 0 <-  .wait(200); !get_when_available.

+!give(Owner,beer): not too_much(Owner, beer)
   <- !go_to(owner,fridge);
   		.print("WENT TO FRIDGE");
      !get_when_available(beer);
	  .print("GOT BEER");
	  !get_when_available(pincho);
	  .print("GOT PINCHO");
      close(fridge);
      !go_to(owner,owner);
      hand_in(Owner,beer);
	  hand_in(Owner,pincho);
	  .print("HANDED IN");
      // remember that another beer has been consumed
      .date(YY,MM,DD); .time(HH,NN,SS);
      .send(robot,tell,consumed(Owner,YY,MM,DD,HH,NN,SS,beer)).
+!give(Owner,beer): too_much(Owner,beer) <- .print("I am not allowed to drink more beer today :(").
-!give(_,_)
   :  true
   <- .current_intention(I);
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).
	  
/** Si no hay cerveza en la nevera se queda esperando a que haya **/
+!get_when_available(beer) : available(fridge, beer) & available(fridge, beer, N) <- .wait(100); open(fridge); get(beer); .send(robot, tell, available(fridge, beer, N-1)).
+!get_when_available(beer) : not available(fridge, beer) <- .wait({ +available(fridge,beer) }); !get_when_available(beer). 

+has(owner,beer) : true
   <- !drink(beer).
-has(owner,beer) : true
   <- !get(beer).

// if I have not beer finish, in other case while I have beer, sip
+!drink(beer) : not has(owner,beer) & .random(Rand) & Rand < 0.5
   <- .send(robot, tell, plate(dirty)); !go_to(owner, bin); recycle(owner,beer);!go_to(owner,owner).
+!drink(beer) : not has(owner,beer)
   <- .send(robot, tell, plate(dirty)); drop(beer).
+!drink(beer) //: has(owner,beer)
   <- sip(beer);
   	  nam(pincho);
     !drink(beer).
-!drink(beer) <- .print("ERROR DRINK OWNER").

+!check_bored : true
   <- .random(X); .wait(X*5000+2000);   // i get bored at random times
      .send(robot, askOne, time(_), R); // when bored, I ask the robot about the time
      .print(R);
      !check_bored.

+msg(M)[source(Ag)] : true
   <- .print("Message from ",Ag,": ",M);
      -msg(M).

