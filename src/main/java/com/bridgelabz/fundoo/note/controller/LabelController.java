package com.bridgelabz.fundoo.note.controller;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.fundoo.note.dto.LabelDto;
import com.bridgelabz.fundoo.note.model.Label;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.note.service.LabelService;
import com.bridgelabz.fundoo.response.Response;

@PropertySource("classpath:message.properties")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LabelController {
	
	static final Logger logger = LoggerFactory.getLogger(LabelController.class);
	
	@Autowired
	private LabelService labelService;
	
	@PostMapping("/createLabel")
	public ResponseEntity<Response> createLabel(@RequestBody LabelDto labelDto, @RequestHeader("token") String token) {
		logger.info("labelDto"+labelDto);
		logger.info("Token"+token);
		logger.trace("Create label");
		Response response = labelService.createLabel(labelDto, token);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteLabel/{labelId}")
	public ResponseEntity<Response> deleteLabel(@PathVariable Long labelId, @RequestHeader("token") String token) {
		logger.info("labelId"+labelId);
		logger.info("Token"+token);
		Response response = labelService.deleteLabel(labelId, token);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}
	
	@GetMapping("/getAllLabels")
	public ResponseEntity<List<Label>> getAllLabels(@RequestHeader("token") String token) {
		logger.info("Token"+token);
		List<Label> labels = labelService.getAllLabels(token);
		return new ResponseEntity<>(labels, HttpStatus.OK);
	}
	
	@PostMapping("/updateLabel/{labelId}")
	public ResponseEntity<Response> updateLabel(@RequestHeader("token") String token, @PathVariable Long labelId, @RequestBody LabelDto labelDto) {
		logger.info("Token"+token);
		logger.info("LabelId"+labelId);
		logger.info("LabelDto"+labelDto);
		Response response = labelService.updateLabel(token, labelId, labelDto);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping("/addLabelInNote/{labelId}/{noteId}")
	public ResponseEntity<Response> addLabelInNote(@RequestHeader("token") String token, @PathVariable Long labelId, @PathVariable Long noteId) {
		logger.info("Add label for note");
		logger.info("Token"+token);
		logger.info("LabelId"+labelId);
		logger.info("NoteId"+noteId);
		//logger.info("LabelDto"+labelDto);
		Response response = labelService.addLabelInNote(token, labelId, noteId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteLabelOfNote/{labelId}/{noteId}")
	public ResponseEntity<Response> deleteLabelFormNote(@RequestHeader("token") String token, @PathVariable Long labelId, @PathVariable Long noteId) {
		logger.info("Delete label from note");
		logger.info("Token"+token);
		logger.info("LabelId"+labelId);
		logger.info("Noteid"+noteId);
		Response response = labelService.deleteLabelFromNote(token, labelId, noteId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/getLabelOfNote/{noteId}")
	public ResponseEntity<Set<Label>> getLabelOfNote(@RequestHeader("token") String token, @PathVariable Long noteId) {
		logger.info("Get label of note");
		logger.info("Token"+token);
		logger.info("NoteId"+noteId);
		Set<Label> labels = labelService.getLabelOfNote(token, noteId);
		return new ResponseEntity<>(labels, HttpStatus.OK);
	}
	
	@GetMapping("/getNoteOfLabel/{labelId}")
	public ResponseEntity<List<Note>> getNoteOfLabel(@RequestHeader("token") String token, @PathVariable Long labelId) {
		logger.info("Get note of Label");
		logger.info("Token"+token);
		logger.info("LabelId"+labelId);
		List<Note> notes = labelService.getNoteOfLabel(token, labelId);
		return new ResponseEntity<>(notes, HttpStatus.OK);
	}
}
