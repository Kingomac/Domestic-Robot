name(lidl).

stock(beer, 10).
price(beer, 5).


money(100000).

last_order_id(1). // initial belief

!start.
+!start : price(beer, P) <- .send(robot, tell, price(beer,P)).

+!inquire(beer, price)[source(Ag)] : price(beer, P) <- .send(Ag, tell, price(beer,P)).



// plan to achieve the goal "order" for agent Ag
+!order(Product,Qtd)[source(Ag)] : stock(beer, Numero_cerves) & Numero_cerves >= Qtd
  <- ?last_order_id(N);
     OrderId = N + 1;
     -+last_order_id(OrderId);
	 +stock(beer, Numero_cerves - Qtd);
	 -stock(beer, Numero_cerves);
     deliver(Product,Qtd);
     .send(Ag, tell, delivered(Product,Qtd,OrderId)).
	 
+!order(Product,Qtd)[source(Ag)] : true 
   <- 	?stock(beer, Numero_cerves);
   		.print("Lo siento, solo nos quedan " , Numero_cerves, " estrellas");
		.send(Ag,tell,stock(Numero_cerves)).

+payment(_, _, P) : money(M) <- -+money(P + M).
		
+msg(M)[source(Ag)] : true
   <- .print("Message from ",Ag,": ",M);
      -msg(M).

