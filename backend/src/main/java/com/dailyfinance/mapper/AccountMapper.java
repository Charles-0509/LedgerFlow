package com.dailyfinance.mapper;

import com.dailyfinance.model.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AccountMapper {
    @Select("""
            SELECT * FROM account
            WHERE user_id = #{userId} AND is_deleted = 0
            ORDER BY is_default DESC, id ASC
            """)
    List<Account> listByUser(@Param("userId") Long userId);

    @Select("""
            SELECT * FROM account
            WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0
            LIMIT 1
            """)
    Account findById(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            SELECT * FROM account
            WHERE user_id = #{userId} AND is_deleted = 0
            ORDER BY is_default DESC, id ASC
            LIMIT 1
            """)
    Account findPreferred(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO account (user_id, name, type, balance, is_default)
            VALUES (#{userId}, #{name}, #{type}, #{balance}, #{isDefault})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Account account);

    @Update("""
            UPDATE account
            SET name = #{name}, type = #{type}, balance = #{balance}, is_default = #{isDefault}
            WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0
            """)
    int update(Account account);

    @Update("UPDATE account SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefault(@Param("userId") Long userId);

    @Update("UPDATE account SET is_deleted = 1, is_default = 0 WHERE id = #{id} AND user_id = #{userId}")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);
}
