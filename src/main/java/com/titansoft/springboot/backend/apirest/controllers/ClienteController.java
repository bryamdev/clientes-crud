package com.titansoft.springboot.backend.apirest.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.titansoft.springboot.backend.apirest.models.entity.Cliente;
import com.titansoft.springboot.backend.apirest.models.service.IClienteService;

//CrossOrigin establece los dominios desde los cuales se puede acceder a este controlador...
//..para enviar y recibir datos
//@CrossOrigin(origins = {"http://localhost:4200", "http://192.168.0.15:4200", "http://192.168.0.11:4200"})
@CrossOrigin(origins = {"*", "http://localhost:4200"})
@RestController
@RequestMapping("/api")
public class ClienteController {

	@Autowired
	private IClienteService clienteService;
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	int contador = 0;
	@GetMapping("/clientes")
	public List<Cliente> getClientes(){
		//log.info("Consultando listado No: " + contador++);
		return clienteService.findAll();
	}
	
	@GetMapping("/clientes/{id}")
	public ResponseEntity<?> getCliente(@PathVariable(name = "id") Long id) {

		Cliente cliente = null;
		Map<String, Object> response = new HashMap<>();

		try{
			cliente = clienteService.findById(id);
		}catch(DataAccessException e){
			response.put("mensaje", "Error al realizar la consulta");
			response.put("error", e.getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if(cliente == null){
			response.put("mensaje", "No existe un cliente en la db con id: " + id);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);
	}
	
	@PostMapping("/clientes")
	//@ResponseStatus(HttpStatus.CREATED) //Retorna el codigo de estado 201 al cliente al llamarse este metodo
	public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente, BindingResult result) {

		Cliente clienteNew = null;
		Map<String, Object> response = new HashMap<>();

		if(result.hasErrors()){

			/*
			List<String> errors = new ArrayList<>();
			
			for(FieldError error : result.getFieldErrors()){
				errors.add("Campo '" + error.getField() + "': " + error.getDefaultMessage());
			}
			*/

			//Forma con flujo de datos(Stream)
			//map: intercepta el flujo, obtiene cada elemento (FieldError), lo convierte y retorna (String)...
			//... generando un Stram de Strings
			//collect: convierte el Stream(String) en una coleccion(List)
			List<String> errors = result.getFieldErrors()
					.stream()
					.map(field -> {
						return "Campo '" + field.getField() + "': " + field.getDefaultMessage();
					})
					.collect(Collectors.toList());

			response.put("mensaje", "Error en la validacion");
			response.put("errors", errors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		try{
			clienteNew = clienteService.save(cliente);
			response.put("mensaje", "Cliente creado con exito");
			response.put("cliente", clienteNew);
		}catch(DataAccessException e){
			response.put("mensaje", "Error al intentar guardar el cliente");
			response.put("error", e.getMessage() + ", " + e.getMostSpecificCause());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	
	@PutMapping("/clientes/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result,@PathVariable(name = "id") Long id) {

		Cliente clienteActual = clienteService.findById(id);
		Cliente clienteUpdated = null;

		Map<String, Object> response = new HashMap<>();

		if(result.hasErrors()){
			List<String> errors = result.getFieldErrors()
					.stream()
					.map(field -> {
						return "Campo '" + field.getField() + "': " + field.getDefaultMessage();
					})
					.collect(Collectors.toList());

			response.put("mensaje", "Error en la validacion");
			response.put("errors", errors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if(clienteActual == null){
			response.put("mensaje", "Error al actualizar, no existe un cliente con id: " + id + " en la BD!");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		
		try{
			clienteActual.setNombre(cliente.getNombre());
			clienteActual.setApellido(cliente.getApellido());
			clienteActual.setEmail(cliente.getEmail());

			clienteUpdated = clienteService.save(clienteActual);

			response.put("mensaje", "Cliente actualizado correctamente!");
			response.put("cliente", clienteUpdated);

		}catch(DataAccessException e){
			response.put("mensaje", "Error al intentar actualizar el cliente");
			response.put("error", e.getMessage() + ", " + e.getMostSpecificCause());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	
	@DeleteMapping("/clientes/{id}")
	public ResponseEntity<?> delete(@PathVariable(name="id") Long id) {

		Map<String, Object> response = new HashMap<>();

		try{
			clienteService.delete(id);
			response.put("mensaje", "El cliente se eliminó satisfactoriamente!");
		}catch(DataAccessException e){
			response.put("mesaje", "Error al intentar eliminar el cliente con id: " + id);
			response.put("error", e.getMessage() + ", " + e.getMostSpecificCause());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		//forma estatica de generar el ResponseEntity
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	
	
}
