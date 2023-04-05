# Ejercicio 1 (Semana 6)

Hicimos dos supermercados, el Mercadona y el Lidl.

1. `supermarket_lidl.asl`.
```prolog
name(lidl).

stock(beer, 10).
price(beer, 5).


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
   <- 	?beer_stock(Numero_cerves);
   		.print("Lo siento, solo nos quedan " , Numero_cerves, " estrellas");
		.send(Ag,tell,stock(Numero_cerves)).

```

2. `supermarket_mercadona.asl`.
```prolog
name(mercadona).

stock(beer, 5).
price(beer, 10).


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
   <- 	?beer_stock(Numero_cerves);
   		.print("Lo siento, solo nos quedan " , Numero_cerves, " estrellas");
		.send(Ag,tell,stock(Numero_cerves)).

```

3. Solo se diferencian en los precios y el nombre. A mayores hicimos un evento `+inquire` para que el robot le pueda preguntar el precio al supermercado.
4. Añadimos creencias y evento `!start` en el que se compruebe cuál es el supermercado más barato.
```prolog
min_price(beer, 100000000, none).

!start.
+!start : true <- .wait(500); for (price(beer, P)[source(S)]) {
	?min_price(beer, MIN, _);
	if (P < MIN){
		-+min_price(beer, P, S);
	}
}.
```
5. Cambiamos el plan de `!bring(owner,beer)` para que haga el pedido al supermecado más barato.
```prolog
+!bring(owner,beer)
   :  not available(beer,fridge) & not trash(X,Y) & min_price(beer, _, S)
   <- .send(S, achieve, order(beer,3));
      !go_to(robot,delivery). // go to fridge and wait there.
```



