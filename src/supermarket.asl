/*name(mercadona).*/

/** Multiplicadores del precio **/
price_rise(S + 1.1) :- .random(S).
price_lower(0.999 - S * 0.1) :- .random(S).

last_order_id(1). // initial belief

/** Inicializar supermercado con variables aleatorias **/
!start.

+!start : true <-
		.random(RandProf); +profit(RandProf); // profit_real <- profit + 1
		.random(M); +money((M + 100) * 1000);
		.wait(proveedor(_,_));
		for(proveedor(Prod,Precio)) {
			.print("OFFER: ", Prod);
			?profit(Profit);
			.random(S);
			+offer(Prod,Precio * (Profit + 1), math.round((S + 1) * 5));
			.send(robot, tell, offer(Prod,Precio * Profit, math.round((S + 1) * 5)));
		}; !make_discount.


/** Cada cierto tiempo hace un descuento **/
+!make_discount : price_lower(Mult) <-
		.wait(10000);
		?profit(P);
		-+profit(P * Mult);
		!make_discount. 

// plan to achieve the goal "order" for agent Ag
+!order(Product,Qtd)[source(Ag)] : offer(Product, Precio, Stock) & Stock >= Qtd
  <- ?last_order_id(N);
     OrderId = N + 1;
     -+last_order_id(OrderId);
	 -+offer(Product, Precio, Stock - Qtd);
     deliver(Product,Qtd);
     .send(Ag, tell, delivered(Product,Qtd,OrderId)).
	 
+!order(Product,Qtd)[source(Ag)] : true 
   <- 	?offer(Product, Precio, Numero_cerves);
   		.print("Lo siento, solo nos quedan " , Numero_cerves, " ", Product);
		.send(Ag,tell,stock(Product, Numero_cerves)).
		
/** Si se queda sin stock lo recarga **/
+offer(Producto, Precio, Cantidad) : Cantidad <= 0 & 
	money(Money) & proveedor(Producto, PrecioProv) & Money >= PrecioProv <-
	-+money(Money - PrecioProv * 10); ?profit(Profit); -+offer(Producto, PrecioProv * Profit, Cantidad + 10).

/** Cuando recibe un pago incrementa el precio de la cerveza **/
+payment(_, _, P) : money(M) & price_rise(MULT) & profit(Profit) <- -+money(P + M); -+profit(Profit * MULT).

		
+msg(M)[source(Ag)] : true
   <- .print("Message from ",Ag,": ",M);
      -msg(M).

