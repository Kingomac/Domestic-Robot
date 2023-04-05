# Ejercicio 2 (Semana 6)

1. Añadimos creencias `money(N)` a los agentes robot y supermercados.
2. Cambiamos el plan de añadir la creencia `delivered`
```prolog
+delivered(beer,Qtd,OrderId): min_price(beer, P, S) & money(M) & M > P
  <- get(delivery);
  	.concat("Pedido recibido con éxito: beer(", OrderId, "), ", Qtd, " unidades e importe ", P, " robux", Mensaje);
  	.send(S, tell, msg(Mensaje));
	.send(S, tell, payment(beer, Qtd, P)); 
	-+money(M - P);
  	.drop_intention(bring(owner,beer));
  	!go_to(robot, fridge);
  	open(fridge);
	save(beer);
	close(fridge);
  	+available(beer,fridge);
	!bring(owner,beer).
```

3. Añadimos creencias `payment` a los supermercados para que se realicen los pagos y el supermercado los gestione. De esta forma le quedarán los pagos a modo de factura.
```prolog
+payment(_, _, P) : money(M) <- -+money(P + M).
```


