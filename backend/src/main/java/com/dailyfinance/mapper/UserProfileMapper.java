package com.dailyfinance.mapper;

import com.dailyfinance.model.UserProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserProfileMapper {
    @Select("SELECT * FROM user_profile WHERE user_id = #{userId} LIMIT 1")
    UserProfile findByUserId(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO user_profile (user_id, default_monthly_budget, currency, default_account_id)
            VALUES (#{userId}, #{budget}, #{currency}, #{defaultAccountId})
            """)
    int insert(@Param("userId") Long userId,
               @Param("budget") BigDecimal budget,
               @Param("currency") String currency,
               @Param("defaultAccountId") Long defaultAccountId);

    @Update("""
            UPDATE user_profile
            SET default_monthly_budget = #{defaultMonthlyBudget},
                currency = #{currency},
                default_account_id = #{defaultAccountId}
            WHERE user_id = #{userId}
            """)
    int update(UserProfile profile);
}
