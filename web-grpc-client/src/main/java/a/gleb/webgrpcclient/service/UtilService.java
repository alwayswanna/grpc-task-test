package a.gleb.webgrpcclient.service;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class UtilService {

    public String generateName(String fileOriginalName){
        int i = fileOriginalName.lastIndexOf('.');
        if (i == -1){
            throw new RuntimeException("RuntimeException: Incorrect file format");
        }else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
            String fileTimeTrack = dateFormat.format(new Date());
            String name = fileOriginalName.substring(0, fileOriginalName.lastIndexOf("."));
            String extension = fileOriginalName.substring(fileOriginalName.lastIndexOf(".") + 1);
            return name + "_" + fileTimeTrack + "." + extension;
        }
    }

}
