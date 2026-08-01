package com.efgenbosh.backend.controller;
import com.efgenbosh.backend.domain.Part;
import com.efgenbosh.backend.dto.car.PartRequest;
import com.efgenbosh.backend.dto.car.PartResponse;
import com.efgenbosh.backend.repository.PartRepository;
import com.efgenbosh.backend.repository.RepairCaseRepository;
import com.efgenbosh.backend.repository.RepairCaseHistoryRepository;
import com.efgenbosh.backend.domain.RepairCaseHistory;
import com.efgenbosh.backend.domain.RepairCaseStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/v1/cars/{carId}/repair-cases/{caseId}/parts")
public class RepairCasePartController {
    private final PartRepository parts; private final RepairCaseRepository cases; private final RepairCaseHistoryRepository history;
    public RepairCasePartController(PartRepository parts,RepairCaseRepository cases,RepairCaseHistoryRepository history){this.parts=parts;this.cases=cases;this.history=history;}
    @GetMapping public List<PartResponse> list(@PathVariable Long carId,@PathVariable Long caseId){ensureCase(carId,caseId);return parts.findAllByRepairCase_IdOrderBySortOrderAscIdAsc(caseId).stream().map(value->PartResponse.from(value, LocalDate.now())).toList();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public PartResponse create(@PathVariable Long carId,@PathVariable Long caseId,@Valid @RequestBody PartRequest request,org.springframework.security.core.Authentication authentication){var repairCase=ensureCase(carId,caseId);ensureEditable(repairCase);Part part=new Part();part.setCar(repairCase.getCar());part.setRepairCase(repairCase);apply(part,request);var saved=parts.save(part);syncStatus(repairCase, userId(authentication));record(repairCase,"Добавлена деталь: " + saved.getName(), userId(authentication));return PartResponse.from(saved,LocalDate.now());}
    @PutMapping("/{partId}") public PartResponse update(@PathVariable Long carId,@PathVariable Long caseId,@PathVariable Long partId,@Valid @RequestBody PartRequest request,org.springframework.security.core.Authentication authentication){var repairCase=ensureCase(carId,caseId);ensureEditable(repairCase);Part part=parts.findById(partId).filter(value->value.getRepairCase()!=null&&value.getRepairCase().getId().equals(caseId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Запчасть не найдена."));apply(part,request);var saved=parts.save(part);syncStatus(repairCase, userId(authentication));record(repairCase,"Изменена деталь: " + saved.getName(), userId(authentication));return PartResponse.from(saved,LocalDate.now());}
    @DeleteMapping("/{partId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long carId,@PathVariable Long caseId,@PathVariable Long partId){var repairCase=ensureCase(carId,caseId);ensureEditable(repairCase);Part part=parts.findById(partId).filter(value->value.getRepairCase()!=null&&value.getRepairCase().getId().equals(caseId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Запчасть не найдена."));parts.delete(part);parts.flush();syncStatus(repairCase, null);}
    private com.efgenbosh.backend.domain.RepairCase ensureCase(Long carId,Long caseId){return cases.findById(caseId).filter(value->value.getCar().getId().equals(carId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Страховой случай не найден."));}
    private void ensureEditable(com.efgenbosh.backend.domain.RepairCase item){if(RepairCaseStatus.CLOSED.code().equals(item.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Закрытый страховой случай доступен только для просмотра.");}
    private void syncStatus(com.efgenbosh.backend.domain.RepairCase item, Long userId){
        if (!RepairCaseStatus.CREATED.code().equals(item.getStatus()) && !RepairCaseStatus.WAITING_PARTS.code().equals(item.getStatus()) && !RepairCaseStatus.PARTS_RECEIVED.code().equals(item.getStatus())) return;
        var related = parts.findAllByRepairCase_IdOrderBySortOrderAscIdAsc(item.getId());
        String next = related.isEmpty() ? RepairCaseStatus.CREATED.code() : related.stream().allMatch(Part::isReceived) ? RepairCaseStatus.PARTS_RECEIVED.code() : (RepairCaseStatus.CREATED.code().equals(item.getStatus()) ? RepairCaseStatus.CREATED.code() : RepairCaseStatus.WAITING_PARTS.code());
        if (!next.equals(item.getStatus())) { String previous = item.getStatus(); item.setStatus(next); cases.save(item); record(item, "Статус деталей: " + previous + " → " + next, userId); }
    }
    private void apply(Part part,PartRequest request){part.setLegacyId(request.legacyId());part.setName(request.name().trim());part.setArticle(request.article()==null?"":request.article().trim());part.setCatalogNumber(request.catalogNumber()==null?"":request.catalogNumber().trim());part.setManufacturer(request.manufacturer()==null?"":request.manufacturer().trim());part.setQuantity(request.quantity()==null?java.math.BigDecimal.ONE:request.quantity());part.setOrderedAt(request.orderedAt());part.setSupplierId(request.supplierId());part.setExpectedDate(request.expectedDate());part.setComment(request.comment()==null?"":request.comment().trim());part.setReceived(request.received());part.setReceivedAt(request.received()? (part.getReceivedAt()==null?LocalDate.now():part.getReceivedAt()) : null);part.setSortOrder(request.sortOrder()==null?0:request.sortOrder());}
    private void record(com.efgenbosh.backend.domain.RepairCase item,String comment,Long userId){RepairCaseHistory event=new RepairCaseHistory();event.setRepairCase(item);event.setNewStatus(item.getStatus());event.setComment(comment);event.setCreatedBy(userId);history.save(event);}
    private Long userId(org.springframework.security.core.Authentication authentication){Object principal=authentication==null?null:authentication.getPrincipal();return principal instanceof com.efgenbosh.backend.security.AppUserPrincipal user?user.getUserId():null;}
}
