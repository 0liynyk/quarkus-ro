package io.quarkus.hibernate.orm.deployment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.hibernate.orm.runtime.boot.QuarkusPersistenceUnitDefinition;
import io.quarkus.hibernate.orm.runtime.boot.xml.RecordableXmlMapping;
import io.quarkus.hibernate.orm.runtime.config.DatabaseOrmCompatibilityVersion;
import io.quarkus.hibernate.orm.runtime.integration.HibernateOrmIntegrationStaticDescriptor;
import io.quarkus.hibernate.orm.runtime.migration.MultiTenancyStrategy;

/**
 * Not to be confused with PersistenceXmlDescriptorBuildItem, which holds
 * items of the same type.
 * This build item represents a later phase, and might include the implicit
 * configuration definitions that are automatically defined by Quarkus.
 */
public final class PersistenceUnitDescriptorBuildItem extends MultiBuildItem {

    private final ParsedPersistenceXmlDescriptor descriptor;

    // The default PU in Hibernate Reactive is named "default-reactive" instead of "<default>",
    // but everything related to configuration (e.g. getAllPersistenceUnitConfigsAsMap() still
    // use the name "<default>", so we need to convert between those.
    private final String configurationName;
    private final Optional<String> dataSource;
    private final Optional<String> dbKind;
    private final MultiTenancyStrategy multiTenancyStrategy;
    private final String multiTenancySchemaDataSource;
    private final List<RecordableXmlMapping> xmlMappings;
    private final Map<String, String> quarkusConfigUnsupportedProperties;
    private final DatabaseOrmCompatibilityVersion databaseOrmCompatibilityVersion;
    private final boolean isReactive;
    private final boolean fromPersistenceXml;

    public PersistenceUnitDescriptorBuildItem(ParsedPersistenceXmlDescriptor descriptor, String configurationName,
            Optional<String> dbKind,
            List<RecordableXmlMapping> xmlMappings,
            Map<String, String> quarkusConfigUnsupportedProperties,
            DatabaseOrmCompatibilityVersion databaseOrmCompatibilityVersion,
            boolean isReactive, boolean fromPersistenceXml) {
        this(descriptor, configurationName,
                Optional.of(DataSourceUtil.DEFAULT_DATASOURCE_NAME), dbKind, MultiTenancyStrategy.NONE, null,
                xmlMappings, quarkusConfigUnsupportedProperties, databaseOrmCompatibilityVersion,
                isReactive, fromPersistenceXml);
    }

    public PersistenceUnitDescriptorBuildItem(ParsedPersistenceXmlDescriptor descriptor, String configurationName,
            Optional<String> dataSource, Optional<String> dbKind,
            MultiTenancyStrategy multiTenancyStrategy, String multiTenancySchemaDataSource,
            List<RecordableXmlMapping> xmlMappings,
            Map<String, String> quarkusConfigUnsupportedProperties,
            DatabaseOrmCompatibilityVersion databaseOrmCompatibilityVersion,
            boolean isReactive, boolean fromPersistenceXml) {
        this.descriptor = descriptor;
        this.configurationName = configurationName;
        this.dataSource = dataSource;
        this.dbKind = dbKind;
        this.multiTenancyStrategy = multiTenancyStrategy;
        this.multiTenancySchemaDataSource = multiTenancySchemaDataSource;
        this.xmlMappings = xmlMappings;
        this.quarkusConfigUnsupportedProperties = quarkusConfigUnsupportedProperties;
        this.databaseOrmCompatibilityVersion = databaseOrmCompatibilityVersion;
        this.isReactive = isReactive;
        this.fromPersistenceXml = fromPersistenceXml;
    }

    public Collection<String> getManagedClassNames() {
        return descriptor.getManagedClassNames();
    }

    public String getExplicitSqlImportScriptResourceName() {
        return descriptor.getProperties().getProperty("jakarta.persistence.sql-load-script-source");
    }

    public String getPersistenceUnitName() {
        return descriptor.getName();
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public Optional<String> getDataSource() {
        return dataSource;
    }

    public MultiTenancyStrategy getMultiTenancyStrategy() {
        return multiTenancyStrategy;
    }

    public String getMultiTenancySchemaDataSource() {
        return multiTenancySchemaDataSource;
    }

    public boolean hasXmlMappings() {
        return !xmlMappings.isEmpty();
    }

    public boolean isFromPersistenceXml() {
        return fromPersistenceXml;
    }

    public QuarkusPersistenceUnitDefinition asOutputPersistenceUnitDefinition(
            List<HibernateOrmIntegrationStaticDescriptor> integrationStaticDescriptors) {
        return new QuarkusPersistenceUnitDefinition(descriptor, configurationName, dataSource, dbKind,
                multiTenancyStrategy, xmlMappings,
                quarkusConfigUnsupportedProperties, databaseOrmCompatibilityVersion,
                isReactive, fromPersistenceXml, integrationStaticDescriptors);
    }
}
