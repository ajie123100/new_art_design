package com.artdesign.system.mapper;

import com.artdesign.system.domain.entity.SysPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
    @Select("""
            SELECT COUNT(*)
            FROM sys_user_post
            WHERE post_id = #{postId}
            """)
    Long countUsersByPostId(@Param("postId") Long postId);
}
