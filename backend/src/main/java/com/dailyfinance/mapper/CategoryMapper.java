package com.dailyfinance.mapper;

import com.dailyfinance.model.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("""
            <script>
            SELECT * FROM category
            WHERE user_id = #{userId} AND is_deleted = 0
            <if test="type != null and type != ''">
              AND type = #{type}
            </if>
            ORDER BY type ASC, is_system DESC, id ASC
            </script>
            """)
    List<Category> list(@Param("userId") Long userId, @Param("type") String type);

    @Select("""
            SELECT * FROM category
            WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0
            LIMIT 1
            """)
    Category findById(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO category (user_id, name, type, icon, color, is_system)
            VALUES (#{userId}, #{name}, #{type}, #{icon}, #{color}, #{isSystem})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("""
            UPDATE category
            SET name = #{name}, type = #{type}, icon = #{icon}, color = #{color}
            WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0
            """)
    int update(Category category);

    @Update("UPDATE category SET is_deleted = 1 WHERE id = #{id} AND user_id = #{userId}")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);
}
