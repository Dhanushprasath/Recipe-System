package com.recipe.backend.controller;

import com.recipe.backend.service.VoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/voice")
public class VoiceController {

    private final VoiceService voiceService;

    // ---------- TEXT → SPEECH ----------
    @PostMapping("/text-to-speech")
    public ResponseEntity<byte[]> textToSpeech(
            @RequestParam String text,
            @RequestParam(defaultValue = "en-US") String lang
    ) throws Exception {

        byte[] audio = voiceService.textToSpeech(text, lang);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(audio);
    }

    // ---------- SPEECH → TEXT ----------
    @PostMapping("/speech-to-text")
    public ResponseEntity<String> speechToText(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "en-US") String lang
    ) throws Exception {

        String text = voiceService.speechToText(file, lang);
        return ResponseEntity.ok(text);
    }
}









