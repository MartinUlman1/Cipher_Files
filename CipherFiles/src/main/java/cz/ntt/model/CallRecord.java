package cz.ntt.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement
public class CallRecord {
    private int callId;
    private long callTime;
    private int callDuration;
    private String fileName;
    private List<UserData> userDataIdlist;

}
