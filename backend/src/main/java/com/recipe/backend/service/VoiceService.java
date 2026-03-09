package com.recipe.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface VoiceService {

    byte[] textToSpeech(String text, String languageCode) throws Exception;

    String speechToText(MultipartFile audioFile, String languageCode) throws Exception;
}
