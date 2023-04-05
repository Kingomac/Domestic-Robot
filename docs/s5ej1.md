# Ejercicio 1 (Semana 5)

Mejorar el código del agente de supermarket para que gestione su stock. Inicialmente debe tener X botellas de cerveza (poner un valor adecuado 100, 40, ...); y según se entreguen cervezas al robot este valor debe disminuir.

Por supuesto, el agente supermarket solo puede atender un pedido cuando tiene suficiente cerveza en su stock, de lo contrario debe informar al robot que no tiene suficiente cerveza para su pedido e indicarle cuántas cervezas tiene.

1. Añadimos la creencia `beer_stock` al supermarket.
```
beer_stock(2).
```
2. Modificamos el plan `order` para que tenga en cuenta si hay o no stock y actue en consecuencia.
```
+!order(Product,Qtd)[source(Ag)] : beer_stock(Numero_cerves) & Numero_cerves >= Qtd
  <- ?last_order_id(N);
     OrderId = N + 1;
     -+last_order_id(OrderId);
	 +beer_stock(Numero_cerves - Qtd);
	 -beer_stock(Numero_cerves);
     deliver(Product,Qtd);
     .send(Ag, tell, delivered(Product,Qtd,OrderId)).
	 
+!order(Product,Qtd)[source(Ag)] : true 
   <- 	?beer_stock(Numero_cerves);
   		.print("Lo siento, solo nos quedan " , Numero_cerves, " estrellas");
		.send(Ag,tell,stock(Numero_cerves)).
```



