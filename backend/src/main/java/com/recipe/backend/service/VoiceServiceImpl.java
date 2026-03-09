package com.recipe.backend.service;

import com.google.cloud.speech.v1.*;
        import com.google.cloud.texttospeech.v1.*;
        import com.recipe.backend.service.VoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class VoiceServiceImpl implements VoiceService {

    @Override
    public byte[] textToSpeech(String text, String languageCode) throws Exception {

        try (TextToSpeechClient client = TextToSpeechClient.create()) {

            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            AudioConfig config = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, config);

            return response.getAudioContent().toByteArray();
        }
    }

    @Override
    public String speechToText(MultipartFile audioFile, String languageCode) throws Exception {

        try (SpeechClient speechClient = SpeechClient.create()) {

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setLanguageCode(languageCode)
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(audioFile.getBytes()))
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);

            StringBuilder transcription = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                transcription.append(result.getAlternatives(0).getTranscript());
            }

            return transcription.toString();
        }
    }
}
