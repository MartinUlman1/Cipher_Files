package cz.ntt.service;

import cz.ntt.dao.UrlRepo;
import cz.ntt.dao.UserRepository;
import cz.ntt.factory.Encrypt;
import cz.ntt.factory.SFTPFactory;
import cz.ntt.model.CallRecord;
import cz.ntt.util.DateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@Data
public class Report {

    private LocalDate periodFrom;
    private LocalDate periodTo;

    private final UserRepository userRepository;

    private final SFTPFactory sftpFactory;

    private final UrlRepo urlRepo;

    private final Encrypt encrypt;

    public static String reportedDate = "";

    @Value("${ntt.fieldName}")
    private String fieldName;

    @Value("${ntt.fieldValue}")
    private List<String> fieldValue;

    @Value("${output.file-path}")
    private String outputFilePath;

    @Value("${input.media-suffix}")
    private String suffix;

    @Value("${sftp.local-directory}")
    private String localFilePathSftp;

    @Value("${sftp.remote-directory}")
    private String remoteFilePathSftp;

    @Value("${encrypt.inputFile}")
    private String enryptInputPath;

    @Value("${encrypt.outputFile}")
    private String encryptOutputPath;

    @Value("${encrypt.outputFileAesKey}")
    private String outputFileAesKey;

    @Value("${encrypt.outputFileVector}")
    private String outputFileVector;

    @Value("${encrypt.inputFilePublicKey}")
    private String inputFilePublicKey;

    public static boolean runningInTestMode;

    public Report(UserRepository userRepository, ApplicationArguments args, SFTPFactory sftpFactory, UrlRepo urlRepo,
                  Encrypt encrypt) {
        this.userRepository = userRepository;
        this.sftpFactory = sftpFactory;
        this.urlRepo = urlRepo;
        this.encrypt = encrypt;

        for (String arg : args.getSourceArgs()) {
            log.info("Arg : {}", arg);

            if (arg.equals("-t")) {
                log.info("Running in test mode!");
                runningInTestMode = true;
                continue;
            }

            if (args.containsOption("date")) {
                if (!DateUtils.isValidDate(String.valueOf(args.getOptionValues("date").get(0)))) {
                    log.error("Invalid date format: {} correct format is --date=yyyy-MM-dd",
                            args.getOptionValues("date").get(0));
                    System.exit(0);
                }
                periodFrom = LocalDate.parse(DateUtils.getFormattedDateAsString(LocalDate.parse(args.
                        getOptionValues("date").get(0))));
                periodTo = periodFrom.plusDays(1);
            }
        }
    }

    @PostConstruct
    public void runDocument() {
        try {
            log.info("Report is being started");

            if (periodFrom == null && periodTo == null) {
                periodFrom = LocalDate.parse(DateUtils.getFormattedDateAsString(LocalDate.now().minusDays(1)));
                periodTo = LocalDate.now();
            }

            log.info("Use date : {} - {}", periodFrom, periodTo);

            List<CallRecord> callRecordList = getCallRecordsByDate(periodFrom, periodTo);

            if (callRecordList.isEmpty()) {
                log.error("No call Records found");
                return;
            }
            log.info("Found : {} call records", callRecordList.size());

            for (CallRecord callRecord : callRecordList) {
                callRecord.setUserDataIdlist(userRepository.getUserData(callRecord.getCallId()));
                writeDataToXml(callRecord, outputFilePath + callRecord.getFileName().replace(suffix,
                        ".xml"));
                urlRepo.getRecodings(Paths.get(outputFilePath, callRecord.getFileName()));
            }

            log.info("The Call recordings have been successfully exported to : {}", outputFilePath);
            encrypt.encryptOAEP(enryptInputPath, encryptOutputPath, inputFilePublicKey, outputFileVector, outputFileAesKey);
            sftpFactory.uploadFileToSFTP(localFilePathSftp, remoteFilePathSftp);
            log.info("The Call recordings have been successfully uploaded to SFTP server: {}", remoteFilePathSftp);
            log.info("The process has been successfully completed. Application closed.");

        } catch (Exception e) {
            log.error("failed with error : {}", e.getStackTrace());
        } finally {
            clearFolders(new File(outputFilePath));
            clearFolders(new File(encryptOutputPath));
        }
    }

    /**
     * @param fromDate Start of a specific day
     * @param toDate   End of a specific day
     * @return Data and metadata about users from the database
     */
    public List<CallRecord> getCallRecordsByDate(LocalDate fromDate, LocalDate toDate) {
        List<CallRecord> userData = userRepository.getCallRecords(fromDate, toDate, fieldName, fieldValue);
        for (CallRecord data : userData) {
            data.setUserDataIdlist(userRepository.getUserData(data.getCallId()));
        }
        return userData;
    }

    /**
     * @param data     What data should be converted to xml format
     * @param fileName Specifies the name of the output folder and converts the suffix .mp3 to .xml
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public void writeDataToXml(CallRecord data, String fileName) throws JAXBException, FileNotFoundException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CallRecord.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(data, new File(fileName));
    }

    private void clearFolders(File file) {
        try {
            FileUtils.cleanDirectory(file);
        } catch (Exception e){
            log.error("clean Folders unsuccessfully : {}", e);
        }


    }
}


