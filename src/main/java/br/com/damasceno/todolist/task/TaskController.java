package br.com.damasceno.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.damasceno.todolist.Util.Utils;
import br.com.damasceno.todolist.users.IUserRepository;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;
    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {

        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID)idUser);

        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter(taskModel.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início da tarefa deve ser maior que a data atual");
        } 
        if (currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data final da tarefa deve ser maior que a data atual");
        } 
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser menor que a data final da tarefa");
        } 

        var task = taskRepository.save(taskModel);

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        return this.taskRepository.findByIdUser((UUID)idUser);
    }

    //http://localhost:8080/tasks/9829823-9sdfsf-3293823
    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {

        var existingTask = this.taskRepository.findById(id).orElse(null);
        var idUser = request.getAttribute("idUser");

        if (existingTask == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada");
        }

        var userWantingToAlter = this.userRepository.findById((UUID)idUser).orElse(null);
        var userTaskOwner = this.userRepository.findById(existingTask.getIdUser()).orElse(null);

        if (!existingTask.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário " + userWantingToAlter.getUsername()  + " não tem permissão para alterar a tarefa do usuário " + userTaskOwner.getUsername());
        }

        Utils.copyNonNullProperties(taskModel, existingTask);

        var task =  this.taskRepository.save(existingTask);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }
}
