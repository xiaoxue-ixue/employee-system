package com.xiaoxue.employee.mapper;

import com.xiaoxue.employee.entity.Emp;
import com.xiaoxue.employee.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * EmpMapper 单元测试。
 *
 * ★ 这是 JUnit 4（pom 里是 junit 4.13.2），注解和 JUnit 5 不一样：
 *
 *     功能           JUnit 4（本项目）              JUnit 5
 *     ---------------------------------------------------------------
 *     前置           @Before                        @BeforeEach
 *     后置           @After                         @AfterEach
 *     断言           org.junit.Assert.*             org.junit.jupiter.api.Assertions.*
 *     测试方法       public void，不能有参数        可以包级私有
 *
 *   ⚠ 最容易踩的坑：JUnit 4 里写 @BeforeEach【不报错，但根本不执行】。
 *     表现是 setUp 没跑 → session / empMapper 还是 null → 测试里 NullPointerException，
 *     而报错位置指向的是业务代码，看着完全不像注解问题，非常难查。
 *
 * ★ 数据为什么不会被测试污染：
 *     JUnit 为【每个测试方法】都新建一个测试类实例，各跑一遍 @Before / @After。
 *     所以每个方法拿到的都是全新 SqlSession = 全新事务，
 *     写操作只在事务里，@After 里 rollback() → 库里不留痕迹。
 *     于是测试可以随便增删改，跑完 emp 表还是 15 条。
 *
 *   ⚠ 唯一的例外：如果某个测试自己 commit() 了，rollback 就救不回来，
 *     那种测试必须自己在最后把数据删干净。（SpringBoot 阶段用 @Transactional 自动回滚，
 *     就是因为这个版本问题太容易踩。）
 */
public class EmpMapperTest {

    /**
     * 测试专用用户名前缀 ut_ （unit test）。
     * 万一哪次测试崩在中途留下残留，一眼就能认出是测试数据。
     * 注意 emp.username 有唯一约束 uk_emp_username，所以每个测试方法里只能插一条同名的。
     */
    private static final String TEST_USERNAME = "ut_emp_001";

    private SqlSession session;
    private EmpMapper empMapper;

    @Before                     // ★ JUnit 4 是 @Before，不是 @BeforeEach
    public void setUp() {
        session = MyBatisUtil.getSession();
        empMapper = session.getMapper(EmpMapper.class);
    }

    @After
    public void tearDown() {
        // 只读测试时这行是空操作；写测试时它就是「不留痕迹」的保证
        session.rollback();
        session.close();
    }

    // ==================== 一、查询 ====================

    @Test
    public void testSelectAll() {
        assertEquals(15, empMapper.selectAll().size());
    }

    @Test
    public void testSelectById() {
        Emp emp = empMapper.selectById(1L);
        assertNotNull("id=1 应该有数据", emp);
        assertEquals(Long.valueOf(1L), emp.getId());
        assertEquals("zhangsan", emp.getUsername());
    }

    @Test
    public void testSelectByIdNotExist() {
        // 查不到返回 null，而不是抛异常 —— 调用方要按 null 处理
        assertNull(empMapper.selectById(999999L));
    }

    @Test
    public void testSelectByConditionName() {
        List<Emp> list = empMapper.selectByCondition("张", null, null, null);
        assertEquals(1, list.size());
        assertEquals("张三", list.get(0).getName());
    }

    @Test
    public void testSelectByConditionDeptId() {
        assertEquals(6, empMapper.selectByCondition(null, 1L, null, null).size());
    }

    @Test
    public void testSelectByConditionNoMatch() {
        // 所有条件都不匹配 → 空列表，而不是 null（MyBatis 返回空集合，不会返回 null）
        List<Emp> list = empMapper.selectByCondition("查无此人", null, null, null);
        assertNotNull("MyBatis 查不到时返回空集合，不是 null", list);
        assertTrue(list.isEmpty());
    }

    // ==================== 二、分页 ====================

    @Test
    public void testCountByCondition() {
        // 全 null 也要能统计 —— 顺带验证 <where> 在没有条件时不生成 WHERE 关键字
        assertEquals(15L, empMapper.countByCondition(null, null, null, null));
        assertEquals(6L, empMapper.countByCondition(null, 1L, null, null));
        assertEquals(1L, empMapper.countByCondition("张", null, null, null));
    }

    @Test
    public void testPage() {
        long total = empMapper.countByCondition(null, null, null, null);
        List<Emp> page1 = empMapper.selectByConditionWithPage(null, null, null, null, 0, 5);
        assertEquals(15, total);
        assertEquals(5, page1.size());
    }

    @Test
    public void testPageSecondPageDoesNotOverlap() {
        List<Emp> page1 = empMapper.selectByConditionWithPage(null, null, null, null, 0, 5);
        List<Emp> page2 = empMapper.selectByConditionWithPage(null, null, null, null, 5, 5);

        assertEquals(5, page2.size());
        // 只验 size 是验不出 offset 算错的 —— 必须验两页的 id 没有交集
        for (Emp a : page1) {
            for (Emp b : page2) {
                assertNotEquals("两页数据重叠了，offset 算错了", a.getId(), b.getId());
            }
        }
    }

    @Test
    public void testPageBeyondEndReturnsEmpty() {
        // offset 超出总数 → 空列表，不能报错（前端翻到最后一页之后是正常操作）
        List<Emp> empty = empMapper.selectByConditionWithPage(null, null, null, null, 1000, 5);
        assertTrue("超出范围的页应返回空列表", empty.isEmpty());
    }

    @Test
    public void testPageWithCondition() {
        long total = empMapper.countByCondition(null, 1L, null, null);
        List<Emp> page = empMapper.selectByConditionWithPage(null, 1L, null, null, 0, 3);
        // total 是总数（6），不是当前页条数（3）—— 前端要靠 total 算总页数
        assertEquals(6, total);
        assertEquals(3, page.size());
    }

    // ==================== 三、写操作（靠 @After 的 rollback 不污染数据）====================

    @Test
    public void testInsert() {
        Emp e = newTestEmp();

        int rows = empMapper.insert(e);
        assertEquals(1, rows);
        assertNotNull("useGeneratedKeys 没生效，id 没回填", e.getId());

        // 同一个会话里能看到自己【尚未提交】的数据 —— 这正是事务的隔离性
        Emp found = empMapper.selectById(e.getId());
        assertNotNull(found);
        assertEquals(TEST_USERNAME, found.getUsername());
        assertEquals("测试员工", found.getName());
        assertNotNull("create_time 应该由数据库默认值填上", found.getCreateTime());
    }

    @Test
    public void testUpdatePartialKeepsOtherFields() {
        Emp e = newTestEmp();
        empMapper.insert(e);
        Emp before = empMapper.selectById(e.getId());

        // ★ 只设 id 和 job，其余字段全是 null —— 这就是动态 <set> 的考点
        Emp patch = new Emp();
        patch.setId(e.getId());
        patch.setJob("高级测试");
        assertEquals(1, empMapper.update(patch));

        Emp after = empMapper.selectById(e.getId());
        assertEquals("job 应该被改掉", "高级测试", after.getJob());

        // 这几个字段 update 时传的是 null，必须保持原样。
        // 如果哪天有人把动态 <set> 改成了全量 SET，这几行会第一个红。
        assertEquals("username 被清空了", before.getUsername(), after.getUsername());
        assertEquals("name 被清空了", before.getName(), after.getName());
        assertEquals("deptId 被清空了", before.getDeptId(), after.getDeptId());
        assertEquals("entryDate 被清空了", before.getEntryDate(), after.getEntryDate());
    }

    @Test
    public void testDeleteById() {
        Emp e = newTestEmp();
        empMapper.insert(e);
        assertNotNull(empMapper.selectById(e.getId()));

        assertEquals(1, empMapper.deleteById(e.getId()));
        assertNull("删完应该查不到", empMapper.selectById(e.getId()));
    }

    @Test
    public void testSelectAllStillCleanAfterWriteTest() {
        // 兜底自检：写操作之后，表里总数没变（本条测试自己没写数据）
        assertEquals(15, empMapper.selectAll().size());
        assertFalse(
                "库里出现了 ut_ 开头的测试残留，说明 rollback 没生效或某次跑崩在中途",
                empMapper.selectAll().stream()
                        .anyMatch(x -> x.getUsername() != null && x.getUsername().startsWith("ut_")));
    }

    /** 造一条测试用员工；用户名带 ut_ 前缀，万一残留好认 */
    private Emp newTestEmp() {
        Emp e = new Emp();
        e.setUsername(TEST_USERNAME);
        e.setName("测试员工");
        e.setDeptId(1L);
        e.setJob("测试");
        e.setEntryDate(LocalDate.now());
        return e;
    }
}
