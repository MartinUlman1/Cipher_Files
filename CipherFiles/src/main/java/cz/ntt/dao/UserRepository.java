package cz.ntt.dao;

import cz.ntt.model.CallRecord;
import cz.ntt.model.UserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@Slf4j
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     *
     * @param fromDate Since the beginning of what day
     * @param toDate By the end of what day
     * @param fieldName
     * @param fieldValue
     * @return
     */
    public List<CallRecord> getCallRecords(LocalDate fromDate, LocalDate toDate, String fieldName, List<String> fieldValue) {
        String sql =
                " select meta.callId, meta.callTime, meta.callDuration, audio.filename from callMetaTbl meta\n" +
                        " join callMetaExTbl ext on ext.callId = meta.callId\n" +
                        " join callAudioTbl audio on audio.callId = meta.callId\n" +
                        " where dateadd(s,meta.callTime,'1970-01-01') > :fromDate\n"+
                        "and dateadd(s,meta.callTime,'1970-01-01') < :toDate\n"+
                        " and ext.fieldName=:fieldName and ext.fieldValue in (:fieldValue)\n" +
                        " and audio.format=3";

        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("fromDate", fromDate)
                .addValue("fieldName", fieldName).addValue("fieldValue", fieldValue).addValue("toDate", toDate);
        List<CallRecord> callRecords = jdbcTemplate.query(sql, namedParameters, new BeanPropertyRowMapper<>(CallRecord.class));
        return callRecords;
    }

    /**
     *
     * @param callId Specific Id of the subject
     * @return
     */
    public List<UserData> getUserData(int callId) {
        String sql = "select fieldName,fieldValue from callMetaExTbl where callId=:id";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", callId);
        List<UserData> userData = jdbcTemplate.query(sql, namedParameters, new BeanPropertyRowMapper<>(UserData.class));
        return userData;
    }

}
