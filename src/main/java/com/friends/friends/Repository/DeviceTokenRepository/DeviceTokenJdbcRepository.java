package com.friends.friends.Repository.DeviceTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DeviceTokenJdbcRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<Long, List<String>> getTokensGroupedByAccount() {
        String sql = "SELECT account_id, array_agg(device_token) AS tokens FROM device_token GROUP BY account_id";
        Map<Long, List<String>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long accountId = rs.getLong("account_id");
            java.sql.Array sqlArray = rs.getArray("tokens");
            String[] tokens = (String[]) sqlArray.getArray();
            result.put(accountId, Arrays.asList(tokens));
        });
        return result;
    }
}