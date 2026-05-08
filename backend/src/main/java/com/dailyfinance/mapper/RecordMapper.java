package com.dailyfinance.mapper;

import com.dailyfinance.model.RecordView;
import com.dailyfinance.model.TransactionRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface RecordMapper {
    @Select("""
            <script>
            SELECT r.*, c.name AS category_name, c.color AS category_color, a.name AS account_name
            FROM transaction_record r
            LEFT JOIN category c ON r.category_id = c.id
            LEFT JOIN account a ON r.account_id = a.id
            WHERE r.user_id = #{userId} AND r.is_deleted = 0
            <if test="type != null and type != ''">AND r.type = #{type}</if>
            <if test="categoryId != null">AND r.category_id = #{categoryId}</if>
            <if test="startDate != null">AND r.record_date &gt;= #{startDate}</if>
            <if test="endDate != null">AND r.record_date &lt;= #{endDate}</if>
            <if test="keyword != null and keyword != ''">
              AND (r.note LIKE CONCAT('%', #{keyword}, '%') OR c.name LIKE CONCAT('%', #{keyword}, '%') OR a.name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY r.record_date DESC, r.id DESC
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<RecordView> search(@Param("userId") Long userId,
                            @Param("type") String type,
                            @Param("categoryId") Long categoryId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("keyword") String keyword,
                            @Param("offset") int offset,
                            @Param("size") int size);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM transaction_record r
            LEFT JOIN category c ON r.category_id = c.id
            LEFT JOIN account a ON r.account_id = a.id
            WHERE r.user_id = #{userId} AND r.is_deleted = 0
            <if test="type != null and type != ''">AND r.type = #{type}</if>
            <if test="categoryId != null">AND r.category_id = #{categoryId}</if>
            <if test="startDate != null">AND r.record_date &gt;= #{startDate}</if>
            <if test="endDate != null">AND r.record_date &lt;= #{endDate}</if>
            <if test="keyword != null and keyword != ''">
              AND (r.note LIKE CONCAT('%', #{keyword}, '%') OR c.name LIKE CONCAT('%', #{keyword}, '%') OR a.name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long count(@Param("userId") Long userId,
               @Param("type") String type,
               @Param("categoryId") Long categoryId,
               @Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate,
               @Param("keyword") String keyword);

    @Select("""
            SELECT r.*, c.name AS category_name, c.color AS category_color, a.name AS account_name
            FROM transaction_record r
            LEFT JOIN category c ON r.category_id = c.id
            LEFT JOIN account a ON r.account_id = a.id
            WHERE r.id = #{id} AND r.user_id = #{userId} AND r.is_deleted = 0
            LIMIT 1
            """)
    RecordView findViewById(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO transaction_record (user_id, type, amount, category_id, account_id, record_date, note)
            VALUES (#{userId}, #{type}, #{amount}, #{categoryId}, #{accountId}, #{recordDate}, #{note})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TransactionRecord record);

    @Update("""
            UPDATE transaction_record
            SET type = #{type},
                amount = #{amount},
                category_id = #{categoryId},
                account_id = #{accountId},
                record_date = #{recordDate},
                note = #{note}
            WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0
            """)
    int update(TransactionRecord record);

    @Update("UPDATE transaction_record SET is_deleted = 1 WHERE id = #{id} AND user_id = #{userId}")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            SELECT
              COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS income,
              COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS expense
            FROM transaction_record
            WHERE user_id = #{userId}
              AND is_deleted = 0
              AND record_date BETWEEN #{startDate} AND #{endDate}
            """)
    Map<String, Object> sumBetween(@Param("userId") Long userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT c.name AS categoryName, r.type AS type, COALESCE(SUM(r.amount), 0) AS total
            FROM transaction_record r
            LEFT JOIN category c ON r.category_id = c.id
            WHERE r.user_id = #{userId}
              AND r.is_deleted = 0
              AND r.record_date BETWEEN #{startDate} AND #{endDate}
            GROUP BY c.name, r.type
            ORDER BY total DESC
            """)
    List<Map<String, Object>> categoryStats(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT DATE_FORMAT(record_date, '%Y-%m-%d') AS day,
              COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS income,
              COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS expense
            FROM transaction_record
            WHERE user_id = #{userId}
              AND is_deleted = 0
              AND record_date BETWEEN #{startDate} AND #{endDate}
            GROUP BY record_date
            ORDER BY record_date ASC
            """)
    List<Map<String, Object>> trendStats(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}
