package ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper;

import java.sql.JDBCType;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ConfigurationNodeRecordDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.070+02:00", comments="Source Table: configuration_node")
    public static final ConfigurationNodeRecord configurationNodeRecord = new ConfigurationNodeRecord();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.070+02:00", comments="Source field: configuration_node.id")
    public static final SqlColumn<Long> id = configurationNodeRecord.id;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.073+02:00", comments="Source field: configuration_node.institution_id")
    public static final SqlColumn<Long> institutionId = configurationNodeRecord.institutionId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.073+02:00", comments="Source field: configuration_node.template_id")
    public static final SqlColumn<Long> templateId = configurationNodeRecord.templateId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.073+02:00", comments="Source field: configuration_node.owner")
    public static final SqlColumn<String> owner = configurationNodeRecord.owner;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.073+02:00", comments="Source field: configuration_node.name")
    public static final SqlColumn<String> name = configurationNodeRecord.name;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.073+02:00", comments="Source field: configuration_node.description")
    public static final SqlColumn<String> description = configurationNodeRecord.description;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.074+02:00", comments="Source field: configuration_node.type")
    public static final SqlColumn<String> type = configurationNodeRecord.type;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.074+02:00", comments="Source field: configuration_node.status")
    public static final SqlColumn<String> status = configurationNodeRecord.status;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2021-05-17T13:37:54.070+02:00", comments="Source Table: configuration_node")
    public static final class ConfigurationNodeRecord extends SqlTable {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<Long> institutionId = column("institution_id", JDBCType.BIGINT);

        public final SqlColumn<Long> templateId = column("template_id", JDBCType.BIGINT);

        public final SqlColumn<String> owner = column("owner", JDBCType.VARCHAR);

        public final SqlColumn<String> name = column("name", JDBCType.VARCHAR);

        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);

        public final SqlColumn<String> type = column("type", JDBCType.VARCHAR);

        public final SqlColumn<String> status = column("status", JDBCType.VARCHAR);

        public ConfigurationNodeRecord() {
            super("configuration_node");
        }
    }
}