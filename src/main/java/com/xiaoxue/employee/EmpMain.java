package com.xiaoxue.employee;

import com.xiaoxue.employee.entity.Emp;
import com.xiaoxue.employee.mapper.EmpMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EmpMain {
    public static void main(String[] args) throws IOException {

        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);

            try (SqlSession session = factory.openSession()) {
                EmpMapper empMapper = session.getMapper(EmpMapper.class);

                print("1) selectAll", empMapper.selectAll());
                print("2) name='张'", empMapper.selectByCondition("张", null, null, null));
                print("3) deptId=1", empMapper.selectByCondition(null, 1L, null, null));
                print("4) 全 null", empMapper.selectByCondition(null, null, null, null));
            }
        }
    }

    /** 只读操作，不需要 commit */
    private static void print(String title, List<Emp> list) {
        System.out.println("===== " + title + " → " + list.size() + " 条 =====");
        for (Emp e : list) {
            System.out.println("   " + e);
        }
    }
}
