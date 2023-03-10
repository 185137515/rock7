package com.lzb.rock.sharding.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lzb.rock.base.enums.ResultEnum;
import com.lzb.rock.base.exception.BusException;
import com.lzb.rock.base.util.UtilClass;
import com.lzb.rock.base.util.UtilString;
import com.lzb.rock.sharding.aop.annotation.ShardingBroadcast;
import com.lzb.rock.sharding.aop.annotation.ShardingDatabaseRule;
import com.lzb.rock.sharding.aop.annotation.ShardingRule;
import com.lzb.rock.sharding.aop.annotation.ShardingTableRule;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author lzb
 * @Date 2021-1-3 18:29:43
 *
 */
@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "sharding", value = "enabled", havingValue = "true")
public class DataShardingSourceConfig {

	@Value("${sharding.basePackages}")
	String basePackages;

	@Autowired
	BaseDruidDataSourceConfig baseDataSourceConfig;

	@Autowired(required = false)
	List<MasterSlaveRuleConfiguration> masterSlaveRuleConfiguration;

	// https://shardingsphere.apache.org/document/legacy/4.x/document/cn/manual/sharding-jdbc/configuration/config-java/#%E6%95%B0%E6%8D%AE%E5%88%86%E7%89%87
	// https://shardingsphere.apache.org/document/legacy/4.x/document/cn/manual/sharding-jdbc/configuration/config-java/#%E6%95%B0%E6%8D%AE%E5%88%86%E7%89%87
	@Bean
	public DataSource getShardingDataSource() throws Exception {
		if (UtilString.isBlank(basePackages)) {
			throw new BusException(ResultEnum.PAEAM_ERR, "basePackages ????????????");
		}

		// ????????????????????????
		ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

		// ??????????????????
		List<Class<?>> ShardingRules = UtilClass.getClassByAnnotation(basePackages, ShardingRule.class);
		for (Class<?> each : ShardingRules) {
			ShardingRule shardingRule = each.getAnnotation(ShardingRule.class);
			ShardingDatabaseRule databaseRule = each.getAnnotation(ShardingDatabaseRule.class);
			ShardingTableRule tableRule = each.getAnnotation(ShardingTableRule.class);
			TableRuleConfiguration tableRuleConfigs = getTableRuleConfiguration(shardingRule, databaseRule, tableRule);
			// ??????????????????
			shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfigs);
			// ?????????????????????
			shardingRuleConfig.getBindingTableGroups().add(shardingRule.logicTable());
		}

		// ?????????????????????????????????????????????????????????,??????????????????
//		if (UtilString.isNotEmpty(defaultDataSourceName)) {
//			shardingRuleConfig.setDefaultDataSourceName(defaultDataSourceName);
//		}

		// ?????????????????????
		List<Class<?>> shardingBroadcasts = UtilClass.getClassByAnnotation(basePackages, ShardingBroadcast.class);
		for (Class<?> each : shardingBroadcasts) {
			ShardingBroadcast sardingBroadcast = each.getAnnotation(ShardingBroadcast.class);
			if (sardingBroadcast != null) {
				if (UtilString.isNotBlank(sardingBroadcast.logicTable())) {
					shardingRuleConfig.getBroadcastTables().add(sardingBroadcast.logicTable());
					shardingRuleConfig.getTableRuleConfigs()
							.add(getBroadcasTableRuleConfiguration(sardingBroadcast.logicTable(),
									sardingBroadcast.idColumn(), sardingBroadcast.actualDataNodes()));

				} else {
					throw new BusException(ResultEnum.PAEAM_ERR, "???????????????????????????," + each.getName());
				}
			}
		}
		// ??????????????????
//		shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
//				new InlineShardingStrategyConfiguration("tenant_id", "ds_${tenant_id % 3}"));

		// ??????????????????
		// shardingRuleConfig.setDefaultTableShardingStrategyConfig(new
		// StandardShardingStrategyConfiguration("order_id", new
		// ModuloShardingTableAlgorithm()));

		// ???????????????????????????????????????????????????org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator
		// shardingRuleConfig.setDefaultKeyGeneratorConfig(new SnowflakeKeyGenerator);

		// ??????????????????????????????????????????????????????
		if (masterSlaveRuleConfiguration != null && masterSlaveRuleConfiguration.size() > 0) {
			shardingRuleConfig.setMasterSlaveRuleConfigs(masterSlaveRuleConfiguration);
			log.info("sharding ??????????????????");
		}

		// ????????????
		// shardingRuleConfig.setEncryptRuleConfig(getEncryptRuleConfiguration());

		Properties properties = new Properties();
		// ??????SQL
		properties.put("sql.show", true);
		// ?????????inline??????????????????????????????????????????????????????: false
		properties.put("allow.range.query.with.inline.sharding", true);

		Map<String, DataSource> dataSourceMap = baseDataSourceConfig.dataSource();
		DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig,
				properties);
		return dataSource;
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	private static KeyGeneratorConfiguration getKeyGeneratorConfiguration(String logicTable, String idColumn) {
		Properties pro = new Properties();
		pro.put("idColumn", idColumn);
		pro.put("logicTable", logicTable);
		KeyGeneratorConfiguration result = new KeyGeneratorConfiguration("REDIS", idColumn, pro);
//		KeyGeneratorConfiguration result = new KeyGeneratorConfiguration("SNOWFLAKE", idColumn,pro);
		return result;
	}

	/**
	 * ???????????????
	 * 
	 * @param logicTable
	 * @param idColumn
	 * @param actualDataNodes
	 * @param shardingColumn
	 * @param algorithmExpression
	 * @return
	 */
	TableRuleConfiguration getBroadcasTableRuleConfiguration(String logicTable, String idColumn,
			String actualDataNodes) {
		TableRuleConfiguration result = new TableRuleConfiguration(logicTable, actualDataNodes);
		// ???????????????????????????????????????????????????????????????????????????
		result.setKeyGeneratorConfig(getKeyGeneratorConfiguration(logicTable, idColumn));
		return result;
	}

	/**
	 * ???????????????
	 * 
	 * @param logicTable
	 * @param idColumn
	 * @param actualDataNodes
	 * @param shardingColumn
	 * @param algorithmExpression
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	TableRuleConfiguration getTableRuleConfiguration(ShardingRule shardingRule, ShardingDatabaseRule databaseRule,
			ShardingTableRule tableRule) throws Exception {
		TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration(shardingRule.logicTable(),
				shardingRule.actualDataNodes());

		// ????????????
		if (databaseRule != null) {
			if (databaseRule.preciseShardingAlgorithm() == null || databaseRule.preciseShardingAlgorithm().length < 1) {
				tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(
						databaseRule.ruleColumn(), databaseRule.algorithmExpression()));
			} else {
				// ??????????????????
				Class<? extends PreciseShardingAlgorithm<?>>[] shardingAlgorithmArr = databaseRule
						.preciseShardingAlgorithm();

				// PreciseShardingAlgorithm<?> shardingAlgorithm =
				// shardingAlgorithmArr[0].newInstance();

				String className = shardingAlgorithmArr[0].getName();
				PreciseShardingAlgorithm<?> shardingAlgorithm = UtilClass.newInstance(className);

				// ??????????????????
				Class<? extends RangeShardingAlgorithm<?>>[] rangeShardingAlgorithmArr = databaseRule
						.rangeShardingAlgorithm();

				if (rangeShardingAlgorithmArr == null || rangeShardingAlgorithmArr.length < 1) {
					tableRuleConfig.setDatabaseShardingStrategyConfig(
							new StandardShardingStrategyConfiguration(databaseRule.ruleColumn(), shardingAlgorithm));
				} else {
					RangeShardingAlgorithm<?> rangeShardingAlgorithm = rangeShardingAlgorithmArr[0].newInstance();
					tableRuleConfig.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration(
							databaseRule.ruleColumn(), shardingAlgorithm, rangeShardingAlgorithm));

				}

			}
		}

		// ????????????
		if (tableRule != null) {

			if (tableRule.preciseShardingAlgorithm() == null || tableRule.preciseShardingAlgorithm().length < 1) {

				if (UtilString.isBlank(tableRule.algorithmExpression())) {
					log.error("???:{},ShardingTableRule?????????algorithmExpression??????", shardingRule.logicTable());
					throw new BusException(ResultEnum.PAEAM_ERR, "ruleColumn ????????????");
				}

				tableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(
						tableRule.ruleColumn(), tableRule.algorithmExpression()));
			} else {
				Class<? extends PreciseShardingAlgorithm<?>>[] shardingAlgorithmArr = tableRule
						.preciseShardingAlgorithm();

				PreciseShardingAlgorithm<?> shardingAlgorithm = shardingAlgorithmArr[0].newInstance();

				Class<? extends RangeShardingAlgorithm<?>>[] rangeShardingAlgorithmArr = tableRule
						.rangeShardingAlgorithm();
				if (rangeShardingAlgorithmArr == null || rangeShardingAlgorithmArr.length < 1) {
					// ??????=???IN ??????BETWEEN???????????????
					tableRuleConfig.setTableShardingStrategyConfig(
							new StandardShardingStrategyConfiguration(tableRule.ruleColumn(), shardingAlgorithm));
				} else {
					// ??????=???IN ??? BETWEEN ?????????BETWEEN ?????????
					RangeShardingAlgorithm<?> rangeShardingAlgorithm = rangeShardingAlgorithmArr[0].newInstance();
					tableRuleConfig.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(
							tableRule.ruleColumn(), shardingAlgorithm, rangeShardingAlgorithm));
				}

			}

		}

		// ???????????????????????????????????????????????????????????????????????????
		tableRuleConfig.setKeyGeneratorConfig(
				getKeyGeneratorConfiguration(shardingRule.logicTable(), shardingRule.idColumn()));

		return tableRuleConfig;
	}

//	TableRuleConfiguration getTestTableRuleConfiguration(String keyColumn) {

//		TableRuleConfiguration result = new TableRuleConfiguration("demo_test", "ds_${[0,1,2]}.demo_test");
//		result.setTableShardingStrategyConfig(
//				new InlineShardingStrategyConfiguration("tenant_id", "ds_${tenant_id % 3}"));
//		// ???????????????????????????????????????????????????????????????????????????
//		result.setKeyGeneratorConfig(getKeyGeneratorConfiguration(keyColumn));
//		return result;
//	}

}
