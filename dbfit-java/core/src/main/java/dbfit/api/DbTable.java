package dbfit.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import dbfit.util.DbParameterAccessor;
import dbfit.util.NameNormaliser;

public class DbTable implements DbObject {

    private DBEnvironment dbEnvironment;
    private String tableOrViewName;
    private Map<String, DbParameterAccessor> allParams;

    public DbTable(DBEnvironment dbEnvironment, String tableName)
            throws SQLException {
        this.dbEnvironment = dbEnvironment;
        this.tableOrViewName = tableName;
        allParams = dbEnvironment.getAllColumns(tableName);
        if (allParams.isEmpty()) {
            throw new SQLException("Cannot retrieve list of columns for "
                    + tableName + " - check spelling and access rights");
        }
    }

    public PreparedStatement buildPreparedStatement(
            DbParameterAccessor[] accessors) throws SQLException {
        PreparedStatement statement = dbEnvironment
                .buildInsertPreparedStatement(tableOrViewName, accessors);
        for (int i = 0; i < accessors.length; i++) {
            accessors[i].bindTo(statement, i + 1);
        }
        return statement;
    }

    public DbParameterAccessor getDbParameterAccessor(String paramName,
            int expectedDirection) {
        String normalisedName = NameNormaliser.normaliseName(paramName);
        DbParameterAccessor accessor = allParams.get(normalisedName);
        if (null == accessor) {
            throw new RuntimeException(
                    "No such database column or parameter: '" + normalisedName + "'");
        }

        if (accessor.getDirection() == DbParameterAccessor.INPUT
                && expectedDirection == DbParameterAccessor.OUTPUT) {
            accessor = dbEnvironment
                    .createAutogeneratedPrimaryKeyAccessor(accessor);
        }
        return accessor;
    }

    public DBEnvironment getDbEnvironment() {
        return dbEnvironment;
    }

}
