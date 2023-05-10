money(1000).
favorite(beer, estrella).
favorite(pincho, durum).

/* Initial goals */

!tell_preferences.
!get(beer).   // initial goal: get a beer
!check_bored. // initial goal: verify whether I am getting bored

+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe"); .wait(where(Sitio, _, _)); !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) & where(Sitio, DestX,DestY) & at(Tipo, OrigenX, OrigenY) <- movement.NextDirection(OrigenX,OrigenY,DestX,DestY,Tipo,AA); move_robot(Tipo, AA); !go_to(Tipo, Sitio).
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio); .wait(3000); !go_to(Tipo, Sitio).


+!tell_preferences <- for(favorite(Prod, Pref)) {
	.send(robot, tell, favorite(Prod, Pref));
}.
// Darle dinero al robot si se lo pide
+!ask_money(Ag) : money(X) & X > 0 <- .send(Ag, achieve, save_money(X * 0.5)); -+money(X * 0.5).
+!get(beer) : true
   <- .send(robot, tell, bring(beer)).

+has(owner,beer) : true
   <- !drink(beer).
-has(owner,beer) : true
   <- 	///////
   		//drop(beer);
		///////
   		!get(beer).

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

