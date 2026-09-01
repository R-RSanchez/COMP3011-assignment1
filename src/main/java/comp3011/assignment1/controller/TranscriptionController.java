package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.service.TranscriptionService;

@RestController
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @GetMapping("/api/test")
    public String test() {
        return "Backend works";
    }

    @PostMapping("/api/transcribe")
    public String transcribe(@RequestParam("file") MultipartFile file) {
        return transcriptionService.transcribe(file);
    }
}