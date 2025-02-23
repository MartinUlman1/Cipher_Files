package cz.ntt.junit.xml;


import cz.ntt.dao.UserRepository;
import cz.ntt.factory.SFTPFactory;
import cz.ntt.model.CallRecord;
import cz.ntt.service.Report;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

//@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {cz.ntt.service.Report.class})
@ActiveProfiles("test")
public class XmlTest {
    @Autowired
    private Report report;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private SFTPFactory sftpFactory;

    @Test
    public void xmlTestFile() throws IOException, JAXBException {
        CallRecord data = new CallRecord();
        data.setCallDuration(33);
        data.setCallTime(55);
        data.setCallId(2);
        data.setFileName("09843028_434WD.mp3");

        //String filePath = null;
        report.writeDataToXml(data,data.getFileName().replace(report.getSuffix(), ".xml"));

        File xml = new File(data.getFileName());
        Assert.assertTrue(xml.exists());

    }
}
