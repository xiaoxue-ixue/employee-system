package com.xiaoxue.employee.mapper;

import com.xiaoxue.employee.entity.Emp;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmpMapper {

    List<Emp> selectAll();

    Emp selectById(Long id);

    // 多参数必须 @Param，否则 XML 里的 #{name} 找不到对应参数
    List<Emp> selectByCondition(@Param("name") String name,
                                @Param("deptId") Long deptId,
                                @Param("entryDateBegin") LocalDate begin,
                                @Param("entryDateEnd") LocalDate end);

    long countByCondition(@Param("name") String name,
                          @Param("deptId") Long deptId,
                          @Param("entryDateBegin") LocalDate begin,
                          @Param("entryDateEnd") LocalDate end);

    List<Emp> selectByConditionWithPage(@Param("name") String name,
                                        @Param("deptId") Long deptId,
                                        @Param("entryDateBegin") LocalDate begin,
                                        @Param("entryDateEnd") LocalDate end,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

    int insert(Emp emp);

    int update(Emp emp);

    int deleteById(Long id);
}
