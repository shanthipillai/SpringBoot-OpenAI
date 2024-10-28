package guru.springframework.springaiimage.controllers;

import guru.springframework.springaiimage.model.Question;
import guru.springframework.springaiimage.services.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jt, Spring Framework Guru.
 */
@RequiredArgsConstructor
@RestController
public class QuestionController {
   @Autowired
    private final OpenAIService openAIService;

   @Autowired
   ChatModel chatModel;


   // private ChatClient chatClient;
    @Autowired
    private OpenAiAudioSpeechModel openAIAudioSpeechModel;


   @Autowired
   OpenAiAudioTranscriptionModel audioModel;


   //Convert a Text into an Image
    @PostMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImage(@RequestBody Question question) {
        return openAIService.getImage(question);
    }


    //Convert an Image into Text
   @GetMapping("/image-to-text")
    public String generateTextToImage()
    {

        String response=ChatClient.create(chatModel).prompt().user(userSpec -> userSpec.text("Draw a flow chart for this code")
               // .media(MimeTypeUtils.IMAGE_JPEG,new FileSystemResource("C:\\Users\\Admin\\Downloads\\spring-ai-image-3-use-dall-e-3-model\\src\\main\\resources\\img.png"))).call().content();
                .media(MimeTypeUtils.IMAGE_JPEG,new FileSystemResource("C:\\Users\\Admin\\Downloads\\spring-ai-image-3-use-dall-e-3-model\\src\\main\\resources\\img_1.png"))).call().content();
        return response
                ;
    }


    //Convert Audio to Text
    @GetMapping("/audio-to-text")
    public String generateTranscription()
    {
        OpenAiAudioTranscriptionOptions audioOptions=OpenAiAudioTranscriptionOptions.builder()
                .withLanguage("en")
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                .build();
        AudioTranscriptionPrompt prompt=new AudioTranscriptionPrompt(new FileSystemResource("C:\\Users\\Admin\\Downloads\\spring-ai-image-3-use-dall-e-3-model\\src\\main\\resources\\OSR_us_000_0010_8k.wav"));

        AudioTranscriptionResponse response=audioModel.call(prompt);
      return  response.getResult().getOutput();
    }


    //Convert Text to Audio
    @GetMapping("/text-to-audio/{prompt}")
    public ResponseEntity<Resource> generateAudio(@PathVariable("prompt") String prompt)
    {
      OpenAiAudioSpeechOptions options=OpenAiAudioSpeechOptions
              .builder()
              .withModel("tts-1")
              .withSpeed(1.0f)
              .withVoice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
              .build();

        SpeechPrompt speechprompt=new SpeechPrompt(prompt,options);
     SpeechResponse response = openAIAudioSpeechModel.call(speechprompt);
  byte[] speechbytes=   response.getResult().getOutput();
        ByteArrayResource bytearrays=new ByteArrayResource(speechbytes);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
              .contentLength(bytearrays.contentLength())
              .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("whatever.mp3").build().toString()).body(bytearrays);
    }


    //Utilize OpenAI Chatbox
    @GetMapping("/question")
    public String useChatModel(@RequestParam String message)
    {
        return chatModel.call(message);
    }
}
