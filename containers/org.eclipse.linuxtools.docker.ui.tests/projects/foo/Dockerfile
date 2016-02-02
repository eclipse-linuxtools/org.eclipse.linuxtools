FROM jboss/wildfly:9.0.2.Final
MAINTAINER xcoulon@redhat.com

ENV postgres_module_dir=/opt/jboss/wildfly/modules/system/layers/base/postgresql/main/
# add postgresql-jdbc jar and module.xml files
RUN mkdir -p ${postgres_module_dir}
ADD module.xml ${postgres_module_dir}
WORKDIR ${postgres_module_dir}
ADD postgresql-9.4-1201.jdbc41.jar ${postgres_module_dir}

# replace the standalone.xml config file with our custom one
ENV config_dir=/opt/jboss/wildfly/standalone/configuration/
ADD standalone.xml ${config_dir}


