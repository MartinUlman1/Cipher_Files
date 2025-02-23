package cz.ntt.dao;

import cz.ntt.util.DateUtils;
import cz.ntt.util.StringUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;


@Slf4j
@Component
public class UrlRepo {

    private SslContext sslContext;
    private HttpClient httpConnector;
    private WebClient client;

    @Value("${webdav.baseUrl}")
    private String baseUrl;

    @Value("${webdav.userName}")
    private String userName;

    @Value("${webdav.passWord}")
    private String passWord;

    public UrlRepo() {
        try {
            sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (SSLException e) {
            log.error("Cannot create SSL context.");
        }

    }
    @PostConstruct
    public void initWebClient(){
        httpConnector = HttpClient.create().secure(t -> t.sslContext(sslContext));

        HttpClient http = HttpClient.create().wiretap(true);
        client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http))
                .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(userName, passWord))
                .baseUrl(baseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer ->
                                configurer.defaultCodecs()
                                        .maxInMemorySize(10 * 1024)
                        )
                        .build())
                .build();
    }

    /**
     *
     * @param targetFile Where to download mp3 recordings from URL
     * @return Downloaded complete recordings
     */
    public Path getRecodings(Path targetFile) {
        Flux<DataBuffer> data = getRecordingsDataBuffer(targetFile.getFileName().toString(),
                DateUtils.getDateString(StringUtils.parseDateFromString(String.valueOf(targetFile.getFileName())),
                        "yyyy-MM-dd_HH-mm-ss"));
        try {
            WritableByteChannel channel;
            channel = Files.newByteChannel(targetFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            return DataBufferUtils.write(data, channel)
                    .map(DataBufferUtils::release)
                    .doFinally(signalType -> {
                        log.debug("Closing downloaded file");
                        try {
                            channel.close();
                        } catch (IOException e) {
                            log.error("Cannot close file: {}", targetFile);
                        }
                    }).then(Mono.just(targetFile)).block();

        } catch (IOException e) {
            log.error("Error downloading recordings from zoom.", e);
        }
        log.info("Mp3 recording was downloaded successfully");
        return null;
    }

    /**
     *
     * @param fileName File name at URL
     * @param date Date for which the recordings will be downloaded
     * @return Browse Url addresses to specific recordings
     */
    private Flux<DataBuffer> getRecordingsDataBuffer(String fileName, LocalDateTime date) {
        try {
            String month = date.getMonthValue() < 10 ? "0" +
                    date.getMonthValue() : String.valueOf(date.getMonthValue());
            String day = date.getDayOfMonth() < 10 ? "0" +
                    date.getDayOfMonth() : String.valueOf(date.getDayOfMonth());
            String hour = date.getHour() < 10 ? "0" +
                    date.getHour() : String.valueOf(date.getHour());

            return client.get()
                    .uri(uriBuilder -> uriBuilder.path(date.getYear() + "/" + month  + "/" + day + "/" + hour +
                                    "/" + fileName)
                            .build())
                    .retrieve()
                    .bodyToFlux(DataBuffer.class);
        } catch (Exception e) {
            log.error("Exception: {}", e);
        }
        return null;
    }

}
//"005HEBL7EC9L5DFN47UOK2LAES01F4KI_2023-05-20_12-01-24-01CC0246-10151ADC-00000001.mp3"