package com.xiaoxue.employee.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * MyBatis 工具类：SqlSessionFactory 全应用只建一次，之后所有地方都从这里拿会话。
 *
 * 【为什么必须只建一次】
 *   SqlSessionFactory 内部持有 Configuration（所有 XML 都解析完的结果），
 *   还管着一个数据库连接池。每 build 一次就等于重新解析一遍 XML + 新建一个池子，
 *   而旧池子里的连接不会自动回收 —— 多 build 几次就是连接泄漏。
 *   之前 Main / EmpMain 各 build 一次，单跑还行；等测试类也各自 build，
 *   池子就会越开越多。
 *
 * 【为什么用静态代码块，而不是懒加载】
 *   static 块在【类加载时执行且只执行一次】，天然满足「全局唯一」；
 *   而且 JVM 保证类初始化是串行且唯一的，所以这里不需要额外加 synchronized。
 *   如果写成 getInstance() + if (factory == null)，反而要自己处理线程安全。
 */
public class MyBatisUtil {

    private static final SqlSessionFactory FACTORY;

    static {
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            if (in == null) {
                // 配置文件找不到时，MyBatis 后面抛的是隐晦的 NullPointerException。
                // 这里主动拦一道，把问题定位在「文件到底存不存在」这个源头上。
                throw new IllegalStateException(
                        "找不到 mybatis-config.xml，检查 src/main/resources 下是否存在该文件");
            }
            FACTORY = new SqlSessionFactoryBuilder().build(in);
        } catch (IOException e) {
            // static 块里不能抛受检异常，只能包成 ExceptionInInitializerError 扔出去。
            // 它表示「这个类初始化失败了」—— 后续任何一次使用都会立刻失败，
            // 不会被悄悄吞掉，比默默返回 null 安全得多。
            throw new ExceptionInInitializerError(e);
        }
    }

    /** 工具类不该被实例化，把构造器私有化（一个约定俗成的习惯） */
    private MyBatisUtil() {
    }

    /**
     * 拿一个会话。
     *
     * ★ 返回的是【不自动提交】的会话：
     *     - 写操作（insert/update/delete）之后必须自己 commit()，否则程序结束就回滚；
     *     - 单元测试反过来利用这一点：写操作只在事务里，@After 里 rollback() 就不污染数据。
     *   要自动提交用 factory.openSession(true)，但一般不建议 —— 那样就没法把多条 SQL 凑成一个事务了。
     */
    public static SqlSession getSession() {
        return FACTORY.openSession();
    }
}
