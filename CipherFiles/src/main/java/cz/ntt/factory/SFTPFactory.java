package cz.ntt.factory;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import cz.ntt.config.SFTPConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;


@Slf4j
@Component
public class SFTPFactory {

    private final SFTPConfig sftpConfig;


    public SFTPFactory(SFTPConfig sftpConfig) {
        this.sftpConfig = sftpConfig;
    }

    /**
     *
     * @return Credentials for logging in to the SFTP server
     * @throws JSchException
     */
    public ChannelSftp setupJsch() throws JSchException {

        JSch jsch = new JSch();

        log.info("sftp :{}", sftpConfig);
        jsch.setKnownHosts(sftpConfig.getKnownHost());
        jsch.addIdentity(sftpConfig.getPrivateKeyPath(), sftpConfig.getPassPhrase());
        Session jschSession = jsch.getSession(sftpConfig.getUserName(), sftpConfig.getHostName(), sftpConfig.getPort());

        jschSession.connect();
        log.info("connection was successful");

        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    /**
     *
     * @param localFile Folder from which files are used
     * @param remoteFile Where files are sent to which folder on the SFTP server
     * @throws JSchException
     */
    public void uploadFileToSFTP(String localFile, String remoteFile) throws JSchException {
        ChannelSftp channelSftp = setupJsch();
        try {

            channelSftp.connect();

            // Setting the current directory on a remote SFTP server
            channelSftp.cd(remoteFile);

            File localFolder = new File(localFile);
            File[] files = localFolder.listFiles();

            if (files != null) {
                // Browse and upload individual files
                for (File file : files) {
                    if (file.isFile()) {
                        channelSftp.put(file.getAbsolutePath(), file.getName());
                        log.info("File " + file.getName() + " was successfully sent to the SFTP server.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error when sending files to SFTP server: " + e.getMessage());
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }
    }
}
