package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TranscriptionController {

    @GetMapping("/api/test")
    public String test() {
        return "Backend works";
    }

    @PostMapping("/api/transcribe")
    public String transcribe(@RequestParam("file") MultipartFile file) {
        return "Received audio: " + file.getSize() + " bytes";
    }
}