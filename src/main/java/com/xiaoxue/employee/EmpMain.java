package com.xiaoxue.employee;

import com.xiaoxue.employee.entity.Emp;
import com.xiaoxue.employee.mapper.EmpMapper;
import com.xiaoxue.employee.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpMain {
    public static void main(String[] args) {

        // 会话统一从 MyBatisUtil 拿；工厂在类加载时只 build 了一次，这里不再重复建
        try (SqlSession session = MyBatisUtil.getSession()) {
            EmpMapper empMapper = session.getMapper(EmpMapper.class);

            // ==================== 一、查询（只读，不用 commit）====================
            print("1) selectAll", empMapper.selectAll());
            print("2) name='张'", empMapper.selectByCondition("张", null, null, null));
            print("3) deptId=1", empMapper.selectByCondition(null, 1L, null, null));
            print("4) 全 null", empMapper.selectByCondition(null, null, null, null));

            // ==================== 二、员工增删改 ====================
            // session 默认【不自动提交】，所以每个写操作后面都必须 commit()。
            // 反过来说这也正是事务的意义：这几行 SQL 凑成一个事务，
            // 中间任何一步抛异常，整体都不会生效，不会留下改了一半的数据。

            // ---- 5) insert：新增员工，验证自增主键回填 ----
            Emp add = new Emp();
            add.setUsername("test001");
            add.setName("测试员工");
            add.setDeptId(1L);                  // dept 表 id=1 是「技术部」；外键要求这个部门必须存在
            add.setJob("测试");
            add.setEntryDate(LocalDate.now());  // 今天

            int insertRows = empMapper.insert(add);
            System.out.println("5) insert 受影响行数 = " + insertRows
                    + "，回填的自增 id = " + add.getId());
            // id 本来由数据库 AUTO_INCREMENT 生成，Java 这边 new 出来时是 null。
            // XML 里配了 useGeneratedKeys="true" keyProperty="id"，
            // MyBatis 才会把生成的 id 再塞回 add 对象；漏配的话这里打印的就是 null。
            session.commit();   // ★ 不 commit，这行只在当前事务里存在，程序一结束就没了

            Long newId = add.getId();

            // ---- 6) selectById：确认真的查得到 ----
            Emp found = empMapper.selectById(newId);
            System.out.println("6) selectById(" + newId + ") = " + found);
            System.out.println("   查得到吗？ " + (found != null ? "✅ 能" : "❌ 不能"));

            // ---- 7) ★ update 关键验证：只改 job，其余字段一个都不许被动 ----
            // 刻意 new 一个对象，【只设 id 和 job】，其它字段全是 null —— 这是重点。
            // XML 里 <set> + <if> 的意义就在这儿：值为 null 的字段不会拼进 SQL，
            //   最终执行的是 →  UPDATE emp SET job = ? WHERE id = ?
            // 要是 XML 写成全量的 "UPDATE emp SET username=?, name=?, dept_id=?, ..."，
            //   那几个 null 就会把库里原本好好的值统统覆盖成 NULL。
            // 下面 checkPartialUpdate() 就是把「到底有没有被清空」这件事逐字段验一遍。
            Emp before = empMapper.selectById(newId);   // 改之前留个底，一会儿比对用

            Emp patch = new Emp();
            patch.setId(newId);
            patch.setJob("高级测试");

            int updateRows = empMapper.update(patch);
            System.out.println("7) update 受影响行数 = " + updateRows);
            session.commit();

            Emp after = empMapper.selectById(newId);
            System.out.println("   更新后 = " + after);
            checkPartialUpdate(before, after, "高级测试");

            // ---- 8) deleteById：删掉 ----
            int deleteRows = empMapper.deleteById(newId);
            System.out.println("8) delete 受影响行数 = " + deleteRows);
            session.commit();

            // ---- 9) 再查一次，应该没了 ----
            Emp gone = empMapper.selectById(newId);
            System.out.println("9) 删除后 selectById(" + newId + ") = " + gone);
            System.out.println("   应该是 null？ " + (gone == null ? "✅ 是" : "❌ 不是，没删干净"));
        }
    }

    /** 只读操作，不需要 commit */
    private static void print(String title, List<Emp> list) {
        System.out.println("===== " + title + " → " + list.size() + " 条 =====");
        for (Emp e : list) {
            System.out.println("   " + e);
        }
    }

    /**
     * 核对「只设 id 和 job」那次 update 的结果。
     *
     * 验两件事：
     *   ① job 必须变成新值              → 改动到底生效没有
     *   ② 其余字段必须和更新前完全一样   → 有没有被 null 覆盖清空
     *
     * 有问题就一次性全列出来，而不是发现一个就 return —— 一次跑完能看全所有毛病。
     */
    private static void checkPartialUpdate(Emp before, Emp after, String expectJob) {
        List<String> problems = new ArrayList<>();

        if (!Objects.equals(expectJob, after.getJob())) {
            problems.add("job 没更新：期望「" + expectJob + "」，实际「" + after.getJob() + "」");
        }
        // 这几个字段 update 时传的都是 null，被 dynamic <set> 挡住了才应该保持原样
        same(problems, "username",  before.getUsername(),  after.getUsername());
        same(problems, "name",      before.getName(),      after.getName());
        same(problems, "deptId",    before.getDeptId(),    after.getDeptId());
        same(problems, "entryDate", before.getEntryDate(), after.getEntryDate());
        same(problems, "image",     before.getImage(),     after.getImage());

        if (problems.isEmpty()) {
            System.out.println("   ✅ job 已更新，且 username / name / deptId / entryDate 一个都没被清空");
        } else {
            System.out.println("   ❌ 动态 <set> 没起作用，发现 " + problems.size() + " 个问题：");
            for (String p : problems) {
                System.out.println("      - " + p);
            }
        }
    }

    /** 两个值必须相等；不等就把差异记进 problems（收集而不是直接抛，方便一次看全） */
    private static void same(List<String> problems, String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            problems.add(field + " 被覆盖：更新前 = " + before + "，更新后 = " + after);
        }
    }
}
