package a.gleb.webgrpcclient.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileInformation {

    private String filename;
    private String filetype;
    private String dateCreation;

}
