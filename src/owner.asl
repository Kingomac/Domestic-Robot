money(1000).
/* Initial goals */

!get(beer).   // initial goal: get a beer
!check_bored. // initial goal: verify whether I am getting bored

// Darle dinero al robot si se lo pide
+!ask_money(Ag) : money(X) & X > 0 <- .send(Ag, achieve, save_money(X * 0.5)); -+money(X * 0.5).
+!get(beer) : true
   <- .send(robot, achieve, bring(owner,beer)).

+has(owner,beer) : true
   <- !drink(beer).
-has(owner,beer) : true
   <- 	///////
   		drop(beer);
		///////
   		!get(beer).

// if I have not beer finish, in other case while I have beer, sip
+!drink(beer) : not has(owner,beer)
   <- true.
+!drink(beer) //: has(owner,beer)
   <- sip(beer);
     !drink(beer).

+!check_bored : true
   <- .random(X); .wait(X*5000+2000);   // i get bored at random times
      .send(robot, askOne, time(_), R); // when bored, I ask the robot about the time
      .print(R);
      !check_bored.

+msg(M)[source(Ag)] : true
   <- .print("Message from ",Ag,": ",M);
      -msg(M).

