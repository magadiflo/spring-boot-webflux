package com.bolsadeideas.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.bolsadeideas.springboot.webflux.app.models.dao.IProductoDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {
	
	@Autowired
	private IProductoDao productoDao;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		this.mongoTemplate.dropCollection("productos")
			.subscribe();
		
		Flux.just(new Producto("Tv Panasonic LCD", 1800D), 
				new Producto("Lavadora Mave 14Kg.", 980D),
				new Producto("Bicicleta Montañera Aro 26", 590D),
				new Producto("Monitor LG 27'", 750D),
				new Producto("Impresora Multifuncional Epson L350", 990D),
				new Producto("Plancha Oster", 360D),
				new Producto("Plomo", 18D),
				new Producto("Wincha 5m.", 19D),
				new Producto("Regla Nivel", 22D))
		.flatMap(producto -> {
			producto.setCreateAt(new Date());
			return this.productoDao.save(producto);
		})
		.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}

}
