/*name(mercadona).*/

/** Multiplicadores del precio **/
price_rise(S + 1.1) :- .random(S).
price_lower(0.999 - S * 0.1) :- .random(S).

last_order_id(1). // initial belief

/** Inicializar supermercado con variables aleatorias **/
!start.
+!start : true <- .random(P); +price(beer, (P + 1) * 20); .send(robot, tell, price(beer,P));
		.random(M); +money((M + 100) * 1000);
		.random(S); +stock(beer, math.round((S + 1) * 5));
		+initial_price((P + 1) * 20); !make_discount.

/** Cada cierto tiempo hace un descuento **/
+!make_discount : price_lower(Mult) <-
		.wait(10000);
		?price(beer, P);
		-+price(beer, P * Mult);
		!make_discount.

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
		
/** Si se queda sin stock lo recarga **/
+stock(beer, P) : P <= 3 & money(M) <- -+money(beer, M - 50);  -+stock(beer, P + 5).

/** Cuando recibe un pago incrementa el precio de la cerveza **/
+payment(_, _, P) : money(M) & price_rise(MULT) <- -+money(P + M); -+price(beer, P * MULT).

		
+msg(M)[source(Ag)] : true
   <- .print("Message from ",Ag,": ",M);
      -msg(M).

