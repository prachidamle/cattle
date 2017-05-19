/**
 * This class is generated by jOOQ
 */
package io.cattle.platform.core.model.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RevisionTable extends org.jooq.impl.TableImpl<io.cattle.platform.core.model.tables.records.RevisionRecord> {

	private static final long serialVersionUID = 1521568454;

	/**
	 * The singleton instance of <code>cattle.revision</code>
	 */
	public static final io.cattle.platform.core.model.tables.RevisionTable REVISION = new io.cattle.platform.core.model.tables.RevisionTable();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<io.cattle.platform.core.model.tables.records.RevisionRecord> getRecordType() {
		return io.cattle.platform.core.model.tables.records.RevisionRecord.class;
	}

	/**
	 * The column <code>cattle.revision.id</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>cattle.revision.name</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>cattle.revision.account_id</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.Long> ACCOUNT_ID = createField("account_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

	/**
	 * The column <code>cattle.revision.kind</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.String> KIND = createField("kind", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>cattle.revision.uuid</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.String> UUID = createField("uuid", org.jooq.impl.SQLDataType.VARCHAR.length(128).nullable(false), this, "");

	/**
	 * The column <code>cattle.revision.description</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR.length(1024), this, "");

	/**
	 * The column <code>cattle.revision.state</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.String> STATE = createField("state", org.jooq.impl.SQLDataType.VARCHAR.length(128).nullable(false), this, "");

	/**
	 * The column <code>cattle.revision.created</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.util.Date> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP.asConvertedDataType(new io.cattle.platform.db.jooq.converter.DateConverter()), this, "");

	/**
	 * The column <code>cattle.revision.removed</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.util.Date> REMOVED = createField("removed", org.jooq.impl.SQLDataType.TIMESTAMP.asConvertedDataType(new io.cattle.platform.db.jooq.converter.DateConverter()), this, "");

	/**
	 * The column <code>cattle.revision.remove_time</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.util.Date> REMOVE_TIME = createField("remove_time", org.jooq.impl.SQLDataType.TIMESTAMP.asConvertedDataType(new io.cattle.platform.db.jooq.converter.DateConverter()), this, "");

	/**
	 * The column <code>cattle.revision.data</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.util.Map<String,Object>> DATA = createField("data", org.jooq.impl.SQLDataType.CLOB.length(16777215).asConvertedDataType(new io.cattle.platform.db.jooq.converter.DataConverter()), this, "");

	/**
	 * The column <code>cattle.revision.service_id</code>.
	 */
	public final org.jooq.TableField<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.Long> SERVICE_ID = createField("service_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

	/**
	 * Create a <code>cattle.revision</code> table reference
	 */
	public RevisionTable() {
		this("revision", null);
	}

	/**
	 * Create an aliased <code>cattle.revision</code> table reference
	 */
	public RevisionTable(java.lang.String alias) {
		this(alias, io.cattle.platform.core.model.tables.RevisionTable.REVISION);
	}

	private RevisionTable(java.lang.String alias, org.jooq.Table<io.cattle.platform.core.model.tables.records.RevisionRecord> aliased) {
		this(alias, aliased, null);
	}

	private RevisionTable(java.lang.String alias, org.jooq.Table<io.cattle.platform.core.model.tables.records.RevisionRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, io.cattle.platform.core.model.CattleTable.CATTLE, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<io.cattle.platform.core.model.tables.records.RevisionRecord, java.lang.Long> getIdentity() {
		return io.cattle.platform.core.model.Keys.IDENTITY_REVISION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<io.cattle.platform.core.model.tables.records.RevisionRecord> getPrimaryKey() {
		return io.cattle.platform.core.model.Keys.KEY_REVISION_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<io.cattle.platform.core.model.tables.records.RevisionRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<io.cattle.platform.core.model.tables.records.RevisionRecord>>asList(io.cattle.platform.core.model.Keys.KEY_REVISION_PRIMARY, io.cattle.platform.core.model.Keys.KEY_REVISION_IDX_REVISION_UUID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<io.cattle.platform.core.model.tables.records.RevisionRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<io.cattle.platform.core.model.tables.records.RevisionRecord, ?>>asList(io.cattle.platform.core.model.Keys.FK_REVISION__ACCOUNT_ID, io.cattle.platform.core.model.Keys.FK_REVISION__SERVICE_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public io.cattle.platform.core.model.tables.RevisionTable as(java.lang.String alias) {
		return new io.cattle.platform.core.model.tables.RevisionTable(alias, this);
	}

	/**
	 * Rename this table
	 */
	public io.cattle.platform.core.model.tables.RevisionTable rename(java.lang.String name) {
		return new io.cattle.platform.core.model.tables.RevisionTable(name, null);
	}
}
