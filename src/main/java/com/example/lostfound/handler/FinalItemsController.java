package com.example.lostfound.handler;
import com.example.lostfound.config.LostItemService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@Profile("final")
public class FinalItemsController {
    private final LostItemService service;
    public FinalItemsController(LostItemService service) { this.service = service; }
    @GetMapping("/api/v1/lost-items")
    public List<LostItemResponse> list() {
        return service.list().stream().map(LostItemResponse::from).toList();
    }
}
