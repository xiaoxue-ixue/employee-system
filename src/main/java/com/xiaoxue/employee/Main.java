package com.xiaoxue.employee;

import com.xiaoxue.employee.entity.Dept;
import com.xiaoxue.employee.mapper.DeptMapper;
import com.xiaoxue.employee.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

public class Main {
    public static void main(String[] args) {

        // 会话统一从 MyBatisUtil 拿；工厂在类加载时只 build 了一次，这里不再重复建
        try (SqlSession session = MyBatisUtil.getSession()) {
            DeptMapper deptMapper = session.getMapper(DeptMapper.class);

            // 1) 初始条数
            System.out.println("1) 初始部门数 = " + deptMapper.selectAll().size());

            // 2) 新增「财务部」
            Dept add = new Dept();
            add.setName("财务部");
            int insertRows = deptMapper.insert(add);
            System.out.println("2) insert 受影响行数 = " + insertRows
                    + "，回填的自增 id = " + add.getId());
            session.commit();   // ★ 不 commit，数据不会真正入库

            // 3) 应该是 4 条
            System.out.println("3) 新增后部门数 = " + deptMapper.selectAll().size());

            // 4) 改名（复用 add 对象，它的 id 已被回填）
            add.setName("财务部-测试");
            int updateRows = deptMapper.update(add);
            System.out.println("4) update 受影响行数 = " + updateRows);
            session.commit();

            // 5) 按 id 回查，验证改名真的生效
            System.out.println("5) 按 id 回查 = " + deptMapper.selectById(add.getId()));

            // 6) 删除
            int deleteRows = deptMapper.deleteById(add.getId());
            System.out.println("6) delete 受影响行数 = " + deleteRows);
            session.commit();

            // 7) 回到 3 条
            System.out.println("7) 删除后部门数 = " + deptMapper.selectAll().size());
        }
    }
}
