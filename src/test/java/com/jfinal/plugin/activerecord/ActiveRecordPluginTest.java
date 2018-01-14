package com.jfinal.plugin.activerecord;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

public class ActiveRecordPluginTest {

  @Before
  public void before() throws URISyntaxException {
    DruidPlugin dp = new DruidPlugin(
      "jdbc:mysql://localhost:3306/enoa?useUnicode=true&characterEncoding=utf-8&useSSL=false&nullNamePatternMatchesAll=true",
      "root",
      "passwd"
    );
    dp.setInitialSize(3).setMaxActive(10);
    dp.addFilter(new StatFilter());

    WallFilter wall = new WallFilter();
    wall.setDbType("mysql");
    WallConfig wc = new WallConfig();
    wc.setMultiStatementAllow(true);
    wc.setCommentAllow(true);

    wall.setConfig(wc);
    dp.addFilter(wall);

    if (!dp.start())
      throw new RuntimeException("Can not start activerecord plugin");

    ActiveRecordPlugin arp = new ActiveRecordPlugin(dp.getDataSource());
    arp.setShowSql(true);
    arp.setDialect(new MysqlDialect());

    String sqlBasePath = ActiveRecordPluginTest.class.getClassLoader().getResource("").toURI().getPath();

    arp.setBaseSqlTemplatePath(sqlBasePath);
    arp.addSqlTemplate("template.sql");

    if (!arp.start())
      throw new RuntimeException("Can not start activerecord plugin");
  }

  @Test
  public void testActiveRecord() {
    List<Record> records = Db.find(Db.getSql("User.list"));
    System.out.println(records);
  }

}
