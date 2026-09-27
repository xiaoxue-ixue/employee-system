package com.xiaoxue.employee.mapper;

import com.xiaoxue.employee.entity.Dept;
import java.util.List;

public interface DeptMapper {
    List<Dept> selectAll();

    Dept selectById(Long id);

    int insert(Dept dept);

    int update(Dept dept);

    int deleteById(Long id);
}

