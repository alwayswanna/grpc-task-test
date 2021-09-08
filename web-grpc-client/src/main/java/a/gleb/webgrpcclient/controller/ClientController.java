package a.gleb.webgrpcclient.controller;

import a.gleb.webgrpcclient.models.FileInformation;
import a.gleb.webgrpcclient.service.ImageServiceImpl;
import a.gleb.webgrpcclient.service.TextServiceImpl;
import a.gleb.webgrpcclient.service.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
@Slf4j
public class ClientController {

    private final TextServiceImpl textService;
    private final ImageServiceImpl imageService;
    private final UtilService utilService;

    @Autowired
    public ClientController(TextServiceImpl textService, ImageServiceImpl imageService, UtilService utilService) {
        this.textService = textService;
        this.imageService = imageService;
        this.utilService = utilService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String mainPage(Map<String, Object> model) {
        try {
            List<FileInformation> fileInformationList = imageService.checkServerDirectory("Check file on server: " + new Date());
            if (fileInformationList.isEmpty()) {
                log.warn("ServerCheckFiles: server has no files " + new Date());
            }
            model.put("files", fileInformationList);
            return "main";
        } catch (Exception ignored) {
        }
        return "main";
    }

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public String sendMessageToServer(@RequestParam String message,
                                      Map<String, Object> model,
                                      RedirectAttributes attributes) {
        if (!message.equals("")) {
            var responseFromServer = textService.simpleTextRequest(message);
            attributes.addFlashAttribute("response", responseFromServer);
            return "redirect:/";
        } else {
            log.warn("Message is null");
            var s = "Send message: Message is null";
            attributes.addFlashAttribute("error", s);
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/upload_file", method = RequestMethod.POST)
    public String uploadImage(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            var filename = utilService.generateName(file.getOriginalFilename());
            log.info("User upload file: " + file.getOriginalFilename() + " Save file with name: " + filename);
            imageService.sendImage(file.getInputStream(), filename);
            redirectAttributes.addFlashAttribute("uploadDone", "Upload image: success");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("uploadError", "Upload image: error");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/download_file/{filename}", method = RequestMethod.GET)
    public @ResponseBody byte[] downloadFile(@PathVariable String filename) {
        ByteArrayOutputStream byteArrayOutputStream = imageService.downloadFile(filename);
        System.out.println(byteArrayOutputStream.toByteArray());
        return byteArrayOutputStream.toByteArray();
    }

}
