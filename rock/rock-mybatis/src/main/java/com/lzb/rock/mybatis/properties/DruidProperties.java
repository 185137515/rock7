package com.lzb.rock.mybatis.properties;

import java.sql.SQLException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;

import lombok.Data;

/**
 * 默认数据库数据源配置 有需要修改请在配置文件修改
 * 
 * @author lzb
 * 
 *         2019年3月10日 上午11:21:32
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class DruidProperties {

	private String url = "jdbc:mysql://127.0.0.1:3306/rock?autoReconnect=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";

	private String username = "root";

	private String password = "root";
	/**
	 * 数据库驱动
	 */
	private String driverClassName = "com.mysql.cj.jdbc.Driver";

	/**
	 * 数据源类型
	 */
	private String type = "com.alibaba.druid.pool.DruidDataSource";

	/**
	 * 初始化大小
	 */
	private Integer initialSize = 300;
	/**
	 * 最大空闲数
	 */
	private Integer minIdle = 100;
	/**
	 * 最大连接
	 */
	private Integer maxActive = 500;
	/**
	 * 连接出错后再尝试连接connectionErrorRetryAttempts次
	 */
	private Integer connectionErrorRetryAttempts = 3600 * 24;
	/**
	 * 数据库服务宕机自动重连机制 false 表示重连
	 */
	private Boolean breakAfterAcquireFailure = false;

	/**
	 * 连接出错后重试时间间隔 秒
	 */
	private Integer timeBetweenConnectErrorMillis = 60;
	/**
	 * 异步初始化策略
	 */
	private Boolean asyncInit = true;
	/**
	 * 连接等待超时时间,单位毫秒
	 */
	private Integer maxWait = 1000 * 10;

	/**
	 * druid数据库连接的心跳检测,防止数据库断开连接
	 */
	private Boolean keepAlive = true;
	/**
	 * 配置隔多久进行一次检测(检测可以关闭的空闲连接),单位毫秒
	 */
	private Integer timeBetweenEvictionRunsMillis = 1000 * 60 * 5;
	/**
	 * 单位：秒，检测连接是否有效的超时时间
	 */
	private Integer validationQueryTimeout = 3;

//    #设置removeAbandoned="true"时，当连接池连接数到达(getNumIdle() < 2) and (getNumActive() > getMaxActive() - 3)  [空闲的连接小于2并且活动的连接大于(最大连接-3)] 时便会启动连接回收，
//    #那种活动时间超过removeAbandonedTimeout="1800"的连接将会被回收，
//    #同时如果logAbandoned="true"设置为true,程序在回收连接的同时会打印日志。
//    #removeAbandoned是连接池的高级功能，理论上这中配置不应该出现在实际的生产环境，因为有时应用程序执行长事务，可能这种情况下，会被连接池误回收，该种配置一般在程序测试阶段，为了定位连接泄漏的具体代码位置，被开启。
//    #生产环境中连接的关闭应该靠程序自己保证。

	/**
	 * 默认值是 300( 秒 ), 活动连接的最大空闲时间单位秒
	 */
	private Integer removeAbandonedTimeout = 60 * 30;
	/**
	 * 最多等待线程
	 */
	private Integer maxWaithThreadCount = -1;
	/**
	 * 默认值是 false, 超过时间限制是否回收
	 */
	private Boolean removeAbandoned = false;
	/**
	 * 关闭abanded连接时输出错误日志
	 */
	private Boolean logAbandoned = true;
	/**
	 * 配置连接在池中的最小生存时间(单位毫秒),最小为30秒
	 */
	private Integer minEvictableIdleTimeMillis = 1000 * 60 * 10;
	/**
	 * 连接在池中的最大生存时间(单位毫秒)
	 */
	private Integer maxEvictableIdleTimeMillis = 1000 * 60 * 60 * 6;
	/**
	 * SQL查询,用来验证从连接池取出的连接,在将连接返回给调用者之前.如果指定,则查询必须是一个SQL SELECT并且必须返回至少一行记录
	 */
	private String validationQuery = "select user()";
	/**
	 * 指明连接是否被空闲连接回收器(如果有)进行检验.如果检测失败,则连接将被从池中去除.注意:
	 * 设置为true后如果要生效,validationQuery参数必须设置为非空字符串
	 */
	private Boolean testWhileIdle = false;
	/**
	 * 指明是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个.注意:
	 * 设置为true后如果要生效,validationQuery参数必须设置为非空字符串
	 */
	private Boolean testOnBorrow = true;
	/**
	 * 指明是否在归还到池中前进行检验注意: 设置为true后如果要生效,validationQuery参数必须设置为非空字符串
	 */
	private Boolean testOnReturn = false;
	/**
	 * 打开PSCache，并且指定每个连接上PSCache的大小 开启池的prepared statement 池功能
	 */
	private Boolean poolPreparedStatements = false;
	/**
	 * 打开PSCache，并且指定每个连接上PSCache的大小
	 */
	private Integer maxPoolPreparedStatementPerConnectionSize = 20;
	/**
	 * 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
	 */
	private String filters = "log4j,wall,mergeStat";
	/**
	 * 通过connectProperties属性来打开mergeSql功能；慢SQL记录
	 */
	private String connectionProperties = "druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000";
	/**
	 * 合并多个DruidDataSource的监控数据
	 */
	private Boolean useGlobalDataSourceStat = true;

	public void config(DruidDataSource dataSource) {

		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);

		dataSource.setDriverClassName(driverClassName);
		// 定义初始连接数
		dataSource.setInitialSize(initialSize);
		// 最小空闲
		dataSource.setMinIdle(minIdle);
		// 定义最大连接数
		dataSource.setMaxActive(maxActive);
		// 最长等待时间
		dataSource.setMaxWait(maxWait);

		dataSource.setConnectionProperties(connectionProperties);
		dataSource.setUseGlobalDataSourceStat(useGlobalDataSourceStat);
		// 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
		dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

		dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
		// 默认值是 false, 是否清理 removeAbandonedTimeout 秒没有使用的活动连接 , 清理后并没有放回连接池
		dataSource.setRemoveAbandoned(removeAbandoned);
		// 关闭abanded连接时输出错误日志
		dataSource.setLogAbandoned(logAbandoned);
		// 配置一个连接在池中最小生存的时间，单位是毫秒
		dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		dataSource.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setTestWhileIdle(testWhileIdle);
		dataSource.setTestOnBorrow(testOnBorrow);
		dataSource.setTestOnReturn(testOnReturn);
		dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
		dataSource.setBreakAfterAcquireFailure(breakAfterAcquireFailure);
		dataSource.setAsyncInit(asyncInit);
		dataSource.setTimeBetweenConnectErrorMillis(timeBetweenConnectErrorMillis);
		// 打开PSCache，并且指定每个连接上PSCache的大小
		dataSource.setPoolPreparedStatements(poolPreparedStatements);
		dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);

		dataSource.setMaxWaitThreadCount(maxWaithThreadCount);
		dataSource.setKeepAlive(keepAlive);
		dataSource.setValidationQueryTimeout(validationQueryTimeout);

		try {
			dataSource.setFilters(filters);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
