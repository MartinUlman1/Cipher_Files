package cz.ntt.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SFTPConfig {
    @Value("${sftp.hostName}")
    private String hostName;
    @Value("${sftp.port}")
    private Integer port;
    @Value("${sftp.userName}")
    private String userName;
    @Value("${sftp.passWord}")
    private String passWord;
    @Value("${sftp.remote-host}")
    private String remoteHost;
    @Value("${sftp.path-privateKey}")
    private String privateKeyPath;
    @Value("${sftp.passPhrase}")
    private String passPhrase;
    @Value("${jsch.knownHost}")
    private String knownHost;
}
