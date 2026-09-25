package com.dayu.yiyibushe.infra.ai.credential;

import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.dao.mybatis.CredentialMybatisMapper;
import com.dayu.yiyibushe.dao.po.CredentialPO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 凭证数据库存储实现：厂商密钥只从 ai_credential 表读取，作为 {@link CredentialStore} 的主实现。
 * <p>
 * 运行期不读取环境变量、不读取 application-*.properties，也不做本地降级：
 * <ul>
 * <li>查询：只读数据库，库中不存在即视为未配置（由 {@link CredentialManager} 抛业务异常）；</li>
 * <li>写入：直接 upsert 数据库；</li>
 * <li>数据库访问异常时读操作返回 null / 空列表，不回退任何本地来源。</li>
 * </ul>
 * 环境变量/本地配置中的存量密钥仅由 AiAssetDataSeeder 在首次启动时一次性迁入数据库，
 * 迁入之后数据库是唯一来源。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Primary
@Component
public class DbCredentialStore implements CredentialStore {

    private static final Logger log = LogUtilExt.getLogger(DbCredentialStore.class);

    /** 凭证启用状态 */
    private static final String STATUS_ENABLED = "ENABLED";

    @Autowired
    private CredentialMybatisMapper credentialMybatisMapper;

    @Override
    public ApiCredential getCredential(String provider) {
        if (StringUtilExt.isBlank(provider)) {
            return null;
        }
        CredentialPO credentialPO = safeSelectByProvider(provider);
        if (Objects.isNull(credentialPO)) {
            return null;
        }
        return new ApiCredential(credentialPO.getAppKey(), credentialPO.getAppSecret());
    }

    @Override
    public void putCredential(String provider, ApiCredential credential) {
        if (StringUtilExt.isBlank(provider) || Objects.isNull(credential)) {
            return;
        }
        upsert(provider, credential);
    }

    @Override
    public void removeCredential(String provider) {
        if (StringUtilExt.isBlank(provider)) {
            return;
        }
        try {
            credentialMybatisMapper.deleteByProvider(provider);
        } catch (Exception e) {
            LogUtilExt.error(log, "[DbCredential] 删除数据库凭证失败 provider={0}, {1}",
                    provider, e.getMessage(), e);
        }
    }

    @Override
    public List<String> listProviders() {
        List<String> providers = new ArrayList<>();
        try {
            for (CredentialPO credentialPO : credentialMybatisMapper.selectAll()) {
                providers.add(credentialPO.getProvider());
            }
        } catch (Exception e) {
            LogUtilExt.error(log, "[DbCredential] 查询数据库凭证列表失败: {0}", e.getMessage(), e);
        }
        return providers;
    }

    @Override
    public boolean hasCredential(String provider) {
        if (StringUtilExt.isBlank(provider)) {
            return false;
        }
        return Objects.nonNull(safeSelectByProvider(provider));
    }

    /**
     * 按厂商查询，数据库异常时返回 null（不回退本地来源）。
     *
     * @param provider 厂商标识
     * @return 凭证 PO 或 null
     */
    private CredentialPO safeSelectByProvider(String provider) {
        try {
            return credentialMybatisMapper.selectByProvider(provider);
        } catch (Exception e) {
            LogUtilExt.error(log, "[DbCredential] 查询数据库凭证失败 provider={0}, {1}",
                    provider, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 新增或更新数据库凭证。
     *
     * @param provider   厂商标识
     * @param credential 凭证
     */
    private void upsert(String provider, ApiCredential credential) {
        CredentialPO existedPO = safeSelectByProvider(provider);
        if (Objects.isNull(existedPO)) {
            credentialMybatisMapper.insert(buildPO(provider, credential));
            return;
        }
        CredentialPO credentialPO = buildPO(provider, credential);
        credentialPO.setCredentialId(existedPO.getCredentialId());
        credentialMybatisMapper.updateByProvider(credentialPO);
    }

    /**
     * 组装凭证 PO（业务ID外部发号）。
     *
     * @param provider   厂商标识
     * @param credential 凭证
     * @return 凭证 PO
     */
    private CredentialPO buildPO(String provider, ApiCredential credential) {
        CredentialPO credentialPO = new CredentialPO();
        credentialPO.setCredentialId(IdUtil.nextId());
        credentialPO.setProvider(provider);
        credentialPO.setAppKey(credential.getAppKey());
        credentialPO.setAppSecret(credential.getAppSecret());
        credentialPO.setStatus(STATUS_ENABLED);
        return credentialPO;
    }
}
