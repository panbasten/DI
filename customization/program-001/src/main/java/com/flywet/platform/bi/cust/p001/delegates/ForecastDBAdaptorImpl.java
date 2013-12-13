package com.flywet.platform.bi.cust.p001.delegates;

import java.util.List;

import org.pentaho.di.core.exception.KettleDatabaseException;

import com.flywet.platform.bi.cust.p001.db.CustomDatabaseRepositoryBase;
import com.flywet.platform.bi.delegates.model.BIAbstractDbAdaptor;

public class ForecastDBAdaptorImpl extends BIAbstractDbAdaptor implements
		ForecastDBAdaptor {

	@Override
	public Object[] getExtendPredict(long year, long month, long xun)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_DESCRIPTION)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_EXTEND_PREDICT)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_MONTH)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_XUN)
				+ " = ?";
		Object[] params = new Object[3];
		params[0] = Long.valueOf(year);
		params[1] = Long.valueOf(month);
		params[2] = Long.valueOf(xun);

		return getOneRow(sql, params);
	}

	@Override
	public String getDescriptionFromExtendPredict(long year, long month,
			long xun) throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_DESCRIPTION)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_EXTEND_PREDICT)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_MONTH)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_XUN)
				+ " = ?";
		Object[] row = getOneRow(sql, new Object[] { year, month, xun });
		if (row != null) {
			return (String) row[0];
		}
		return null;
	}

	@Override
	public Long getIDFromExtendPredict(long year, long month, long xun)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_ID_EXTEND_PREDICT)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_EXTEND_PREDICT)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_MONTH)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_XUN)
				+ " = ?";

		Object[] row = getOneRow(sql, new Object[] { year, month, xun });

		if (row != null) {
			return (Long) row[0];
		}

		return null;
	}

	@Override
	public List<Object[]> getAllExtendPredict() throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_YEAR)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_MONTH)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_XUN)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_DESCRIPTION)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_EXTEND_PREDICT)
				+ " ORDER BY "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_YEAR)
				+ " DESC , "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_MONTH)
				+ " DESC , "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_XUN)
				+ " DESC";
		return getRows(sql);
	}

	@Override
	public List<Object[]> getAllExtendPredictEva()
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_YEAR)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_MONTH)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S1)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S2)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S3)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S4)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S5)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S6)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S7)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S8)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S9)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_EXTEND_PREDICT_EVA)
				+ " ORDER BY "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_YEAR)
				+ " DESC , "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_MONTH)
				+ " DESC";

		return getRows(sql);
	}

	@Override
	public List<Object[]> getMonthPredictEvaD(long year, long month)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_CITY)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S1)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S2)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S3)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S4)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S5)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S6)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S7)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_S8)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_MONTH_PREDICT_EVA_D)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_MONTH)
				+ " = ?";

		return getRows(sql, new Object[] { year, month });
	}

	@Override
	public void delMonthPredictEvaD(long year, long month)
			throws KettleDatabaseException {
		String delSql = "DELETE FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_MONTH_PREDICT_EVA_D)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_D_MONTH)
				+ " = ?";
		execSql(delSql, new Object[] { year, month });
	}

	@Override
	public Long getIDFromMonthPredictEva(long year, long month)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_ID_MONTH_PREDICT_EVA)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_MONTH_PREDICT_EVA)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_MONTH)
				+ " = ?";

		Object[] row = getOneRow(sql, new Object[] { year, month });
		if (row != null) {
			return (Long) row[0];
		}

		return null;
	}

	@Override
	public Object[] getExtendPredictEva(long year, long month)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S1)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S2)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S3)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S4)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S5)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S6)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S7)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S8)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_S9)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_DESCRIPTION)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_EXTEND_PREDICT_EVA)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_MONTH)
				+ " = ?";

		return getOneRow(sql, new Object[] { year, month });
	}

	@Override
	public Long getIDFromExtendPredictEva(long year, long month)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_ID_MONTH_PREDICT_EVA)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_EXTEND_PREDICT_EVA)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_EXTEND_PREDICT_EVA_MONTH)
				+ " = ?";

		Object[] row = getOneRow(sql, new Object[] { year, month });
		if (row != null) {
			return (Long) row[0];
		}
		return null;
	}

	@Override
	public List<Object[]> getAllMonthPredictEva()
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_YEAR)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_MONTH)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_S1)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_S2)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_S3)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_S4)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_S5)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_S6)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_MONTH_PREDICT_EVA)
				+ " ORDER BY "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_YEAR)
				+ " DESC , "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_EVA_MONTH)
				+ " DESC";

		return getRows(sql);
	}

	@Override
	public List<Object[]> getAllMonthPredictScore()
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_YEAR)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_MONTH)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S1)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S2)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S3)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S4)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S5)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S6)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S7)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_MONTH_PREDICT_SCORE)
				+ " ORDER BY "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_YEAR)
				+ " DESC , "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_MONTH)
				+ " DESC";

		return getRows(sql);
	}

	@Override
	public Object[] getMonthPredictScore(long year, long month)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S1)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S2)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S3)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S4)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S5)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S6)
				+ ","
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_S7)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_MONTH_PREDICT_SCORE)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_MONTH)
				+ " = ?";

		return getOneRow(sql, new Object[] { year, month });
	}

	@Override
	public Long getIDFromMonthPredictScore(long year, long month)
			throws KettleDatabaseException {
		String sql = "SELECT "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_ID_MONTH_PREDICT_SCORE)
				+ " FROM "
				+ quoteTable(CustomDatabaseRepositoryBase.TABLE_C_MONTH_PREDICT_SCORE)
				+ " WHERE "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_YEAR)
				+ " = ? AND "
				+ quote(CustomDatabaseRepositoryBase.FIELD_MONTH_PREDICT_SCORE_MONTH)
				+ " = ?";

		Object[] row = getOneRow(sql, new Object[] { year, month });
		if (row != null) {
			return (Long) row[0];
		}
		return null;
	}
}