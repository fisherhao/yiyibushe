package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.common.util.JsonUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 说明：MyBatis TypeHandler，把流任务上下文 Map 与数据库 JSON 列互转。
 * <p>
 * 写入时把 {@code Map<String,Object>} 序列化成语义 JSON 字符串，读取时反序列化回 Map，
 * 从而让 flow_task.context / task_node.output 这类 JSON 列能在 PO 与数据库间自由映射。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class JsonMapTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    /**
     * 写入：把上下文字典序列化成 JSON 字符串
     *
     * @param preparedStatement
     *                          预编译语句
     * @param index
     *                          参数下标（从 1 开始）
     * @param parameter
     *                          待写入的字典
     * @param jdbcType
     *                          JDBC 类型
     * @throws SQLException
     *                      写入数据库异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int index,
            Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(index, JsonUtilExt.toJsonString(parameter));
    }

    /**
     * 读取（游标方式）：把 JSON 列反序列化成字典
     *
     * @param resultSet
     *                 结果集
     * @param columnName
     *                 列名
     * @return 字典，列为空返回空字典
     * @throws SQLException
     *                      读取数据库异常
     */
    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, String columnName)
            throws SQLException {
        return toMap(resultSet.getString(columnName));
    }

    /**
     * 读取（下标方式）：把 JSON 列反序列化成字典
     *
     * @param resultSet
     *                 结果集
     * @param columnIndex
     *                 列下标（从 1 开始）
     * @return 字典，列为空返回空字典
     * @throws SQLException
     *                      读取数据库异常
     */
    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, int columnIndex)
            throws SQLException {
        return toMap(resultSet.getString(columnIndex));
    }

    /**
     * 存储过程读取：把 JSON 列反序列化成字典
     *
     * @param callableStatement
     *                          存储过程语句
     * @param columnIndex
     *                          列下标（从 1 开始）
     * @return 字典，列为空返回空字典
     * @throws SQLException
     *                      读取数据库异常
     */
    @Override
    public Map<String, Object> getNullableResult(CallableStatement callableStatement, int columnIndex)
            throws SQLException {
        return toMap(callableStatement.getString(columnIndex));
    }

    /**
     * JSON 字符串转字典：空或非法时返回空字典
     *
     * @param jsonText
     *                 JSON 字符串
     * @return 字典
     */
    private Map<String, Object> toMap(String jsonText) {
        if (StringUtilExt.isBlank(jsonText)) {
            return new HashMap<>();
        }
        Map<String, Object> parsedMap = JsonUtilExt.parseObject(jsonText, Map.class);
        return Objects.isNull(parsedMap) ? new HashMap<>() : parsedMap;
    }
}
