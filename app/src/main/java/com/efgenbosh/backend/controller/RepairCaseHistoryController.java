package com.efgenbosh.backend.controller;
import com.efgenbosh.backend.dto.car.RepairCaseHistoryResponse;
import com.efgenbosh.backend.repository.RepairCaseHistoryRepository;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import com.efgenbosh.backend.repository.AppUserRepository;
import com.efgenbosh.backend.repository.RepairCaseStatusRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/v1/cars/{carId}/repair-cases/{caseId}/history")
public class RepairCaseHistoryController {
    private final RepairCaseHistoryRepository history; private final RepairCaseRepository cases; private final AppUserRepository users; private final RepairCaseStatusRepository statusDictionary;
    public RepairCaseHistoryController(RepairCaseHistoryRepository history, RepairCaseRepository cases, AppUserRepository users, RepairCaseStatusRepository statusDictionary){this.history=history;this.cases=cases;this.users=users;this.statusDictionary=statusDictionary;}
    @GetMapping public List<RepairCaseHistoryResponse> list(@PathVariable Long carId,@PathVariable Long caseId){ cases.findById(caseId).filter(value->value.getCar().getId().equals(carId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Страховой случай не найден.")); return history.findAllByRepairCase_IdOrderByCreatedAtDesc(caseId).stream().map(value -> { var response = RepairCaseHistoryResponse.from(value); var name = value.getCreatedBy() == null ? "Система" : users.findById(value.getCreatedBy()).map(user -> user.getName()).orElse("Пользователь #" + value.getCreatedBy()); return new RepairCaseHistoryResponse(response.id(), response.previousStatus(), response.newStatus(), humanize(response.comment()), response.createdAt(), response.createdBy(), name); }).toList(); }
    private String humanize(String comment){ if(comment==null || !comment.startsWith("Статус деталей: ")) return comment; String[] transition=comment.substring("Статус деталей: ".length()).split(" → ",2); return transition.length==2 ? "Статус деталей: " + label(transition[0]) + " → " + label(transition[1]) : comment; }
    private String label(String code){return statusDictionary.findByCodeAndActiveTrue(code).map(value->value.getLabel()).orElse(code);}
}
