package com.platform.config.run;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.platform.config.mapping.PlatformMapping;
import com.platform.constant.ConstantCache;
import com.platform.constant.ConstantInit;
import com.platform.plugin.PropertiesPlugin;
import com.platform.plugin.SqlXmlPlugin;
import com.platform.tools.ToolCache;

/**
 * 独立启动Jfinal中的插件，不依赖web容器调用插件
 * @author DHJ  dongcb678@163.com
 */
public class ConfigCore {

	private static Logger log = Logger.getLogger(ConfigCore.class);
	
    public ConfigCore() throws IOException {
    	Properties properties = new Properties();
    	properties.load(ConfigCore.class.getResourceAsStream("/init_autdatamotion.properties"));
    	PropertiesPlugin propertiesPlugin = new PropertiesPlugin(properties,true);
    	propertiesPlugin.start();
		
		log.info("configPlugin 配置Druid数据库连接池连接属性");
		DruidPlugin druidPlugin = new DruidPlugin(
				(String)PropertiesPlugin.getParamMapValue(ConstantInit.db_connection_jdbcUrl), 
				(String)PropertiesPlugin.getParamMapValue(ConstantInit.db_connection_userName), 
				(String)PropertiesPlugin.getParamMapValue(ConstantInit.db_connection_passWord), 
				(String)PropertiesPlugin.getParamMapValue(ConstantInit.db_connection_driverClass));

		log.info("configPlugin 配置Druid数据库连接池大小");
		druidPlugin.set(
				(Integer)PropertiesPlugin.getParamMapValue(ConstantInit.db_initialSize), 
				(Integer)PropertiesPlugin.getParamMapValue(ConstantInit.db_minIdle), 
				(Integer)PropertiesPlugin.getParamMapValue(ConstantInit.db_maxActive));
		
		log.info("configPlugin 配置ActiveRecord插件");
		ActiveRecordPlugin arp = new ActiveRecordPlugin(ConstantInit.db_dataSource_main, druidPlugin);
		//arp.setTransactionLevel(4);//事务隔离级别
		boolean devMode = Boolean.parseBoolean((String) PropertiesPlugin.getParamMapValue(ConstantInit.config_devMode));
		arp.setDevMode(devMode); // 设置开发模式
		arp.setShowSql(devMode); // 是否显示SQL
		arp.setContainerFactory(new CaseInsensitiveContainerFactory(true));// 大小写不敏感
		
		log.info("configPlugin 数据库类型判断");
		String db_type = (String) PropertiesPlugin.getParamMapValue(ConstantInit.db_type_key);
		if(db_type.equals(ConstantInit.db_type_postgresql)){
			log.info("configPlugin 使用数据库类型是 postgresql");
			arp.setDialect(new PostgreSqlDialect());
			
		}else if(db_type.equals(ConstantInit.db_type_mysql)){
			log.info("configPlugin 使用数据库类型是 mysql");
			arp.setDialect(new MysqlDialect());
			
		}else if(db_type.equals(ConstantInit.db_type_oracle)){
			log.info("configPlugin 使用数据库类型是 oracle");
			druidPlugin.setValidationQuery("select 1 FROM DUAL"); //指定连接验证语句
			arp.setDialect(new OracleDialect());
		}
		
		druidPlugin.start();
		
		log.info("configPlugin 表扫描注册");
		//BaseMapping baseMapping = new BaseMapping();
		PlatformMapping baseMapping = new PlatformMapping();
		baseMapping.setConfigName(ConstantInit.db_dataSource_main);
		baseMapping.setArp(arp);
		//baseMapping.scan(propertiesPlugin);


		
		arp.start();
		
//		log.info("I18NPlugin 国际化键值对加载");
//		I18NPlugin i18NPlugin = new I18NPlugin();
//		i18NPlugin.start();
		
		if(ToolCache.getCacheType().equals(ConstantCache.cache_type_ehcache)){
			log.info("EhCachePlugin EhCache缓存");
			EhCachePlugin ehCachePlugin = new EhCachePlugin();
			ehCachePlugin.start();
			
		}else if(ToolCache.getCacheType().equals(ConstantCache.cache_type_redis)){
			log.info("RedisPlugin Redis缓存");
			String redisIp = (String) PropertiesPlugin.getParamMapValue(ConstantInit.config_redis_ip);
			Integer redisPort = (Integer) PropertiesPlugin.getParamMapValue(ConstantInit.config_redis_port);
			RedisPlugin systemRedis = new RedisPlugin(ConstantCache.cache_name_redis_system, redisIp, redisPort);
			systemRedis.start();
		}
//		
		log.info("SqlXmlPlugin 解析并缓存 xml sql");
		SqlXmlPlugin sqlXmlPlugin = new SqlXmlPlugin();
		sqlXmlPlugin.start();
//		
//		log.info("afterJFinalStart 缓存参数");
//		ParamInitPlugin paramInitPlugin = new ParamInitPlugin();
//		paramInitPlugin.start();
    }

}
