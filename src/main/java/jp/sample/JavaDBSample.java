package jp.sample;

import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
 
@Component
public class JavaDBSample {
  @Autowired
  JdbcTemplate jdbcTemplate;

  @Transactional
  public List<Result> sampleSelect() {
    return jdbcTemplate.query("select"
                              + "  f.id"
                              + " ,f.str as str1"
                              + " ,m.str as str2"
                              + " ,t.str as str3"
                              + " from fixed_data f"
                              + " left join master1 m"
                              + "   on f.id = m.id"
                              + " left join test_data1 t"
                              + "   on m.id = t.id",
      new RowMapper<Result>() {

          @Override
          public Result mapRow(ResultSet rs, int rowNum) throws SQLException {
            Result result = new Result();
            result.setId(rs.getInt("ID"));
            result.setStr1(rs.getString("STR1"));
            result.setStr2(rs.getString("STR2"));
            result.setStr3(rs.getString("STR3"));
            return result;
          }
      });
  }
}
