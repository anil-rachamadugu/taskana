package org.taskana.rest;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.taskana.TaskService;
import org.taskana.TaskanaEngine;
import org.taskana.exceptions.NotAuthorizedException;
import org.taskana.exceptions.TaskNotFoundException;
import org.taskana.model.Task;

@RestController
@RequestMapping(path = "/v1/tasks", produces = { MediaType.APPLICATION_JSON_VALUE })
public class TaskController {

	@Autowired
	TaskanaEngine taskanaEngine;
	@Autowired
	private TaskService taskService;

	@RequestMapping
	public ResponseEntity<List<Task>> getTasks(@RequestParam MultiValueMap<String, String> params)
			throws LoginException {
		try {

			if (params.keySet().size() == 0) {
				return ResponseEntity.status(HttpStatus.OK).body(taskService.getTasks());
			}
			if (params.containsKey("workbasketid") && params.containsKey("state")) {
				System.out.println("HOLGER1");

				return ResponseEntity.status(HttpStatus.OK)
						.body(taskService.getTasksForWorkbasket(params.get("workbasketid"), params.get("state")));
			}
			return ResponseEntity.status(HttpStatus.OK)
					.body(taskService.getTasksForWorkbasket(params.getFirst("workbasketid")));
		} catch (NotAuthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@RequestMapping(value = "/{taskId}")
	public ResponseEntity<Task> getTask(@PathVariable(value = "taskId") String taskId) {
		try {
			Task task = taskService.getTaskById(taskId);
			return ResponseEntity.status(HttpStatus.OK).body(task);
		} catch (TaskNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/claim")
	public ResponseEntity<Task> claimTask(@PathVariable String taskId, @RequestBody String userName) {
		// TODO verify user
		try {
			taskService.claim(taskId, userName);
			Task updatedTask = taskService.getTaskById(taskId);
			return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
		} catch (TaskNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/complete")
	public ResponseEntity<Task> completeTask(@PathVariable String taskId) {
		try {
			taskService.complete(taskId);
			Task updatedTask = taskService.getTaskById(taskId);
			return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
		} catch (TaskNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Task> createTask(@RequestBody Task task) {
		try {
			Task createdTask = taskService.create(task);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/transfer/{workbasketId}")
	public ResponseEntity<Task> transferTask(@PathVariable String taskId, @PathVariable String workbasketId) {
		try {
			Task updatedTask = taskService.transfer(taskId, workbasketId);
			return ResponseEntity.status(HttpStatus.CREATED).body(updatedTask);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
